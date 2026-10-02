"""Start the local review app with an existing V3 export; never downloads weights."""
import argparse
import os
from pathlib import Path


def main():
    parser = argparse.ArgumentParser(description='Start the local subscription review app.')
    parser.add_argument('--model-dir', type=Path, help='The complete final_subscription_model_v3 export folder')
    parser.add_argument('--port', type=int, default=8000)
    args = parser.parse_args()
    root = Path(__file__).resolve().parent
    if args.model_dir:
        folder = args.model_dir.expanduser().resolve()
    elif os.environ.get('SUBSCRIPTION_MODEL_DIR'):
        folder = Path(os.environ['SUBSCRIPTION_MODEL_DIR']).expanduser().resolve()
    elif (root / 'final_subscription_model_v3/configuration.json').is_file():
        folder = root / 'final_subscription_model_v3'
    else:
        folder = Path.home() / 'Desktop/subscription_dataset_v3/final_subscription_model_v3'
    if not (folder / 'configuration.json').is_file():
        parser.error('Model not found. Run: python run_local.py --model-dir "/path/to/final_subscription_model_v3"')
    if not 1 <= args.port <= 65535:
        parser.error('Port must be between 1 and 65535.')
    os.environ['SUBSCRIPTION_MODEL_DIR'] = str(folder)
    print(f'Using your existing model: {folder}')
    print(f'After startup completes, open http://127.0.0.1:{args.port}')
    import uvicorn
    uvicorn.run('main:app', host='0.0.0.0', port=args.port)


if __name__ == '__main__':
    main()
