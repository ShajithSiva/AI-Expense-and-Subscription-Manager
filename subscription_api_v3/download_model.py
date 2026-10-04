import os
from pathlib import Path

from huggingface_hub import snapshot_download

REPO_ID = "Shajith001/subscription-distilbert-v3"
MODEL_DIR = Path("final_subscription_model_v3")

token = os.getenv("HF_TOKEN")

if not token:
    raise RuntimeError("HF_TOKEN environment variable is missing.")

MODEL_DIR.mkdir(parents=True, exist_ok=True)

print(f"Downloading private model: {REPO_ID}")

snapshot_download(
    repo_id=REPO_ID,
    repo_type="model",
    local_dir=str(MODEL_DIR),
    token=token,
)

print(f"Model downloaded successfully to: {MODEL_DIR.resolve()}")