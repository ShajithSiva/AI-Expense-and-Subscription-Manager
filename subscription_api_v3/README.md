# Subscription Review API — V3

புதிய local FastAPI project. V3 prediction-ஐ user review செய்த பிறகு மட்டும் subscription save ஆகும். Browser page மற்றும் `/docs` API tester இரண்டும் உள்ளன. Model weights ZIP-ல் சேர்க்கப்படவில்லை; நீங்கள் ஏற்கெனவே train செய்த `final_subscription_model_v3` folder பயன்படுத்தப்படும்.

## Mac-ல் தொடங்குவது

1. ZIP extract செய்து `subscription_api_v3` folder-ஐ Desktop-ல் வைக்கவும்.
2. V3 model train செய்த அதே Python environment-ஐ Terminal-ல் activate செய்யவும். அந்த environment-ல் `torch` மற்றும் `transformers` ஏற்கெனவே இருக்கும்.
3. Terminal-ல்:

```bash
cd ~/Desktop/subscription_api_v3
python -m pip install -r requirements-api.txt
python run_local.py
```

இந்த launcher உங்கள் `Desktop/subscription_dataset_v3/final_subscription_model_v3` folder-ஐ தானாகக் கண்டுபிடிக்கும். வேறு இடத்தில் இருந்தால் `python run_local.py --model-dir "/your/path/final_subscription_model_v3"` பயன்படுத்தவும். Port 8000 busy என்றால் `--port 8001` சேர்க்கவும்.

உங்கள் training environment-ல் `python3` command பயன்படுத்தினால் மேலுள்ள `python`-க்கு பதில் `python3` பயன்படுத்தவும். Server ஓடும்போது Terminal-ஐ open-ஆக வைத்திருக்கவும். Stop செய்ய Ctrl+C.

4. Browser-ல் திறக்கவும்: http://127.0.0.1:8000
5. Message paste செய்து **Analyze message** click செய்யவும். Result மட்டும் வந்தால் எதுவும் save ஆகாது.
6. Service name, recurring amount, currency, cycle ஆகியவற்றை original message பார்த்து நிரப்பவும். Confirmation checkbox select செய்து **Confirm and save** click செய்தால் மட்டும் record save ஆகும். Subscription இல்லை என்றால் **Do not save** click செய்யவும்.

மாற்றாக complete `final_subscription_model_v3` folder-ஐ API folder-க்குள் copy செய்தால் environment variable தேவையில்லை. Source model files-ஐ மாற்ற வேண்டாம்.

## புதிய Python environment தேவைப்பட்டால்

Python 3.11 அல்லது 3.12 பயன்படுத்தவும்:

```bash
cd ~/Desktop/subscription_api_v3
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -r requirements.txt
export SUBSCRIPTION_MODEL_DIR="$HOME/Desktop/subscription_dataset_v3/final_subscription_model_v3"
python -m uvicorn main:app --host 127.0.0.1 --port 8000
```

Dependency compatibility issue வந்தால் உங்கள் model export-ல் உள்ள `package_versions.json`-ன் torch/transformers versions-ஐ பயன்படுத்தவும். Existing training environment பயன்படுத்துவது simplest option. This project does not train or download a replacement model.

## Expected model structure

```text
final_subscription_model_v3/
  configuration.json
  preprocessing_config.json
  transformer/
    config.json
    model.safetensors
    tokenizer.json
    tokenizer_config.json
    ...other tokenizer files from the export...
```

`SUBSCRIPTION_MODEL_DIR` points to the parent export folder, not `transformer/`. The API reads the saved binary threshold, enforces class 1 = Subscription, uses max_length 256 and the V3 cleanup without vendor masking. Trained weights load once at startup, locally, on CPU. Automatic saving is never enabled, regardless of the probability.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| GET | `/` | Local review page |
| GET | `/docs` | Interactive API documentation |
| GET | `/health` | Readiness and model version |
| POST | `/predict` | Analyze text and create a pending review ID; no subscription saved |
| POST | `/suggestions/{id}/confirm` | Explicitly confirm details and save |
| POST | `/suggestions/{id}/reject` | Mark the suggestion as rejected without saving |
| GET | `/subscriptions` | List confirmed subscriptions |

Predict body:

```json
{"text":"Your Spotify paid membership renews monthly for LKR 1990."}
```

Response includes `suggestion_id`, `predicted_label`, `subscription_probability`, `binary_threshold`, `input_truncated`, `decision: REVIEW_REQUIRED`, and `saved: false`. The probability is not calibrated certainty. There is no automatic amount/service extraction; the user supplies the confirmed details.

Copy the returned suggestion ID into the confirm endpoint and submit:

```json
{
  "confirmed": true,
  "service": "Spotify Premium",
  "amount": "1990.00",
  "currency": "LKR",
  "billing_cycle": "monthly",
  "next_billing_date": null
}
```

`confirmed` must be the JSON boolean `true`, not a string or number. Amount must be finite, positive and have at most two decimal places. Currency is a three-letter uppercase code; verify it manually. Supported cycles: weekly, monthly, quarterly, yearly. Money is stored as a decimal string rather than a floating-point balance.

The user can confirm even if the model predicts Non-Subscription. This supports correcting false negatives. The API trusts the explicit confirmation request; an Android/web client must send it only after the user actually approves.

## Storage and duplicate behavior

SQLite is stored at `data/subscriptions.sqlite3` by default. Set `SUBSCRIPTION_DB` to another file path if required. Records persist across server restarts. SQLite write transactions make confirmation atomic.

- Same suggestion + same confirmed details: returns the saved record, without another insert.
- Same suggestion + different details: returns HTTP 409 instead of silently changing a confirmed record.
- Another prediction for the same cleaned message cannot create a second subscription: HTTP 409.
- This prevents duplicate confirmation/replay of the same message; it is not full merchant/account deduplication. Different messages for the same plan still require the user to check the saved list.
- Rejected suggestions cannot later be confirmed without analyzing again. Confirmed suggestions cannot be rejected via the reject endpoint.

Raw email/SMS text is used in memory for inference but is not saved in the database. The database stores a hash of cleaned text, model score, decision state, timestamps, and user-confirmed details. It does not export training examples or retrain from corrections. The review page does not support editing/deleting existing subscriptions yet.

## Scope

This is a **local, single-user prototype**. It has no accounts, multi-user authorization or cloud deployment. Keep the supplied localhost binding. Authentication and per-user storage are needed before exposing it as a shared/Android backend. No CORS permissions are enabled by default.

Text beyond 256 model tokens is truncated and the response/page explicitly says so. Input limit is 20,000 characters. Missing/incompatible model settings stop startup; inference errors return HTTP 503. The app never falls back to untrained weights or mock scores.

The V3 model still has known errors on inactive plans, offers and non-renewing passes. User confirmation is therefore required for every save. No lifecycle state, recurrence calculation or calibrated auto-add behavior is implemented.

## Tests

```bash
python -m pip install -r requirements-test.txt
python -m pytest tests -q
```

API/database tests inject a fake model only within tests. Optional model-loader tests use a tiny randomly initialized DistilBERT to check offline load, label mapping, threshold reading, token truncation and direct-inference parity; they do not measure your model's accuracy. See `TEST_RESULTS.md` for checks performed during creation.

Implementation references: FastAPI lifespan loading https://fastapi.tiangolo.com/advanced/events/ ; validated request bodies https://fastapi.tiangolo.com/tutorial/body/ ; Hugging Face local model loading https://huggingface.co/docs/transformers/main_classes/model .
