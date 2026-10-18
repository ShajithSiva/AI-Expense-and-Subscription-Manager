"""Load the user's existing DistilBERT subscription model locally.

Supports the documented V3 export (configuration.json + transformer/) and
legacy flat exports (subscription_config.json + model/tokenizer files in root).
No model is downloaded, retrained, or replaced.
"""
import json
import math
import threading
from dataclasses import dataclass
from pathlib import Path

from preprocessing import preprocess_email


@dataclass(frozen=True)
class Score:
    probability: float
    truncated: bool


def _read_json(path: Path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, ValueError) as exc:
        raise RuntimeError(f"Cannot read model configuration: {path}") from exc


def read_configuration(folder: Path):
    """Read and validate either supported export's metadata."""
    folder = Path(folder).expanduser().resolve()
    v3_path = folder / "configuration.json"
    legacy_path = folder / "subscription_config.json"

    if v3_path.is_file():
        config = _read_json(v3_path)
        # This API is specifically for the user's DistilBERT-only V3 model.
        if config.get("dataset_version") != "v3":
            raise RuntimeError("Expected dataset_version='v3' in configuration.json.")
        if config.get("subscription_label_id") != 1:
            raise RuntimeError("Expected subscription_label_id=1 in configuration.json.")
        if config.get("vendor_masking") is not False:
            raise RuntimeError("Expected vendor_masking=false for the V3 preprocessing contract.")
        if config.get("text_weight") != 1.0 or config.get("recurrence_weight") != 0.0:
            raise RuntimeError("Expected the validated text-only V3 configuration (text_weight=1.0, recurrence_weight=0.0).")
        if config.get("max_length") != 256:
            raise RuntimeError("Expected max_length=256 in configuration.json.")
        threshold = config.get("binary_threshold")
        if "binary_threshold" not in config:
            raise RuntimeError("Missing binary_threshold in configuration.json.")
        config = dict(config)
        config["_format"] = "v3"
        config["_threshold"] = threshold
        config["_model_dir"] = folder / "transformer"
        config["_version"] = str(config.get("dataset_version", "v3"))
    elif legacy_path.is_file():
        config = _read_json(legacy_path)
        if config.get("model_type") != "DistilBERT":
            raise RuntimeError("Expected model_type=DistilBERT in subscription_config.json.")
        if config.get("max_length") != 256:
            raise RuntimeError("Expected max_length=256 in subscription_config.json.")
        config = dict(config)
        config["_format"] = "legacy"
        config["_threshold"] = config.get("threshold")
        config["_model_dir"] = folder
        config["_version"] = str(config.get("pipeline_version", "distilbert_only_v1"))
    else:
        raise RuntimeError(
            "Model export configuration not found. Set SUBSCRIPTION_MODEL_DIR to the export folder "
            "containing configuration.json (V3) or subscription_config.json (legacy)."
        )

    threshold = config.get("_threshold")
    if (isinstance(threshold, bool) or not isinstance(threshold, (int, float))
            or not math.isfinite(threshold) or not 0 < threshold < 1):
        raise RuntimeError("Invalid or missing prediction threshold in model configuration.")
    return config


class ModelService:
    def __init__(self, folder: Path):
        folder = Path(folder).expanduser().resolve()
        print(f"Loading existing subscription model from: {folder}")
        self.config = read_configuration(folder)
        self.threshold = float(self.config["_threshold"])
        self.max_length = int(self.config["max_length"])
        self.version = self.config["_version"]
        model_folder = Path(self.config["_model_dir"])

        import torch
        from transformers import AutoModelForSequenceClassification, AutoTokenizer
        self.torch = torch

        for required in ("config.json", "model.safetensors", "tokenizer_config.json", "vocab.txt"):
            if not (model_folder / required).is_file():
                raise RuntimeError(f"Missing {required} in model files directory: {model_folder}")

        # Check the class mapping when the model architecture metadata is available.
        hf_config = _read_json(model_folder / "config.json")
        labels = hf_config.get("id2label", {})
        if labels:
            label_one = str(labels.get("1", labels.get(1, ""))).upper().replace("-", "_")
            if "SUBSCRIPTION" not in label_one or "NON_SUBSCRIPTION" in label_one:
                raise RuntimeError(f"Model class 1 must be SUBSCRIPTION; found id2label[1]={label_one!r}.")

        print(f"Loading tokenizer from: {model_folder}")
        self.tokenizer = AutoTokenizer.from_pretrained(
            model_folder, local_files_only=True, trust_remote_code=False,
        )
        print(f"Loading trained weights from: {model_folder}")
        self.model = AutoModelForSequenceClassification.from_pretrained(
            model_folder, local_files_only=True, trust_remote_code=False,
            use_safetensors=True,
        ).to("cpu")
        self.model.eval()
        self.lock = threading.Lock()
        print("Existing subscription model loaded successfully.")
        print(f"Threshold: {self.threshold}; max_length: {self.max_length}; version: {self.version}")

    def score(self, text: str) -> Score:
        prepared = preprocess_email(text)
        token_count = len(self.tokenizer.encode(prepared, add_special_tokens=True, truncation=False))
        inputs = self.tokenizer(
            prepared, return_tensors="pt", padding=True,
            truncation=True, max_length=self.max_length,
        )
        with self.lock:
            with self.torch.inference_mode():
                outputs = self.model(**inputs)
                probabilities = self.torch.softmax(outputs.logits, dim=-1)
                probability = float(probabilities[0, 1].item())
        if not math.isfinite(probability) or not 0 <= probability <= 1:
            raise RuntimeError("Model returned an invalid probability.")
        return Score(probability=probability, truncated=token_count > self.max_length)
