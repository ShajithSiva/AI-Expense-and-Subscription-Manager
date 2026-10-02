"""Load the existing DistilBERT subscription model locally."""

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


def read_configuration(folder: Path):
    config_path = folder / "subscription_config.json"

    if not config_path.is_file():
        raise RuntimeError(
            f"Missing subscription_config.json in model folder: {folder}"
        )

    try:
        config = json.loads(
            config_path.read_text(encoding="utf-8")
        )
    except (OSError, ValueError) as exc:
        raise RuntimeError(
            f"Cannot read subscription_config.json from {folder}"
        ) from exc

    if config.get("model_type") != "DistilBERT":
        raise RuntimeError(
            "Expected model_type=DistilBERT."
        )

    if config.get("max_length") != 256:
        raise RuntimeError(
            "Expected max_length=256."
        )

    threshold = config.get("threshold")

    if (
        isinstance(threshold, bool)
        or not isinstance(threshold, (int, float))
        or not math.isfinite(threshold)
        or not 0 < threshold < 1
    ):
        raise RuntimeError(
            "Invalid threshold in subscription_config.json."
        )

    return config


class ModelService:

    def __init__(self, folder: Path):

        folder = Path(folder).resolve()

        print(f"Loading DistilBERT model from: {folder}")

        self.config = read_configuration(folder)

        self.threshold = float(
            self.config.get("threshold", 0.1)
        )

        self.max_length = int(
            self.config.get("max_length", 256)
        )

        self.version = self.config.get(
            "pipeline_version",
            "distilbert_only_v1"
        )

        import torch
        from transformers import (
            AutoModelForSequenceClassification,
            AutoTokenizer,
        )

        self.torch = torch

        # The HuggingFace model files are directly
        # inside the supplied model folder.
        if not (folder / "config.json").is_file():
            raise RuntimeError(
                f"Missing config.json in {folder}"
            )

        if not (folder / "model.safetensors").is_file():
            raise RuntimeError(
                f"Missing model.safetensors in {folder}"
            )

        print("Loading tokenizer...")

        self.tokenizer = AutoTokenizer.from_pretrained(
            folder,
            local_files_only=True,
            trust_remote_code=False,
        )

        print("Loading trained DistilBERT weights...")

        self.model = (
            AutoModelForSequenceClassification.from_pretrained(
                folder,
                local_files_only=True,
                trust_remote_code=False,
                use_safetensors=True,
            )
            .to("cpu")
        )

        self.model.eval()

        self.lock = threading.Lock()

        print("DistilBERT model loaded successfully.")
        print(f"Threshold: {self.threshold}")
        print(f"Max length: {self.max_length}")
        print(f"Pipeline version: {self.version}")


    def score(self, text: str) -> Score:

        prepared = preprocess_email(text)

        token_count = len(
            self.tokenizer.encode(
                prepared,
                add_special_tokens=True,
                truncation=False,
            )
        )

        inputs = self.tokenizer(
            prepared,
            return_tensors="pt",
            padding=True,
            truncation=True,
            max_length=self.max_length,
        )

        with self.lock:

            with self.torch.inference_mode():

                outputs = self.model(**inputs)

                probabilities = self.torch.softmax(
                    outputs.logits,
                    dim=-1,
                )

                probability = float(
                    probabilities[0, 1].item()
                )

        if (
            not math.isfinite(probability)
            or not 0 <= probability <= 1
        ):
            raise RuntimeError(
                "Model returned an invalid probability."
            )

        return Score(
            probability=probability,
            truncated=token_count > self.max_length,
        )