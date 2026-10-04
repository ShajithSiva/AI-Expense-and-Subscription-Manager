import os
import subprocess
import sys

from download_model import MODEL_DIR

# Download happens when download_model is imported.
os.environ["SUBSCRIPTION_MODEL_DIR"] = str(MODEL_DIR.resolve())

port = os.getenv("PORT", "10000")

print(f"Using model directory: {os.environ['SUBSCRIPTION_MODEL_DIR']}")
print(f"Starting API on port {port}")

subprocess.run(
    [
        sys.executable,
        "-m",
        "uvicorn",
        "main:app",
        "--host",
        "0.0.0.0",
        "--port",
        port,
    ],
    check=True,
)