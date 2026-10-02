import hashlib
import logging
import math
import os
from contextlib import asynccontextmanager
from datetime import date, datetime, timedelta
from zoneinfo import ZoneInfo
from decimal import Decimal, ROUND_HALF_UP
from pathlib import Path
from typing import Annotated, Literal
from uuid import UUID

from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import FileResponse, JSONResponse
from pydantic import BaseModel, ConfigDict, Field, StrictBool, field_validator

from currency_service import CurrencyService
from extraction import extract_details
from model_service import ModelService
from preprocessing import preprocess_email
from storage import DecisionConflict, MissingRecord, Store

ROOT = Path(__file__).resolve().parent
logger = logging.getLogger('subscription_api')


class InputModel(BaseModel):
    model_config = ConfigDict(extra='forbid', str_strip_whitespace=True, allow_inf_nan=False)


class PredictRequest(InputModel):
    text: Annotated[str, Field(min_length=1, max_length=20000)]


class ConversionRequest(InputModel):
    amount: Annotated[Decimal, Field(gt=0, max_digits=12, decimal_places=2, allow_inf_nan=False)]
    currency: Annotated[str, Field(pattern=r'^[A-Z]{3}$')]


class ConfirmRequest(InputModel):
    confirmed: StrictBool
    service: Annotated[str, Field(min_length=1, max_length=120)]
    amount: Annotated[Decimal, Field(gt=0, max_digits=12, decimal_places=2, allow_inf_nan=False)]
    currency: Annotated[str, Field(pattern=r'^[A-Z]{3}$')]
    billing_cycle: Literal['weekly', 'monthly', 'quarterly', 'yearly']
    next_billing_date: date | None = None

    @field_validator('confirmed')
    @classmethod
    def explicit_confirmation(cls, value):
        if value is not True:
            raise ValueError('Confirm only after the user reviews and approves these details.')
        return value


def create_app(model=None, database_path: Path | None = None, model_dir: Path | None = None, currency_service=None, today_provider=None):
    db = Store(database_path or Path(os.getenv('SUBSCRIPTION_DB', str(ROOT / 'data/subscriptions.sqlite3'))))

    fx = currency_service or CurrencyService(db.path.parent / 'exchange_rates.json')

    @asynccontextmanager
    async def lifespan(app):
        # Load once; startup fails visibly if model files/settings are wrong.
        app.state.model = model if model is not None else ModelService(
            model_dir or Path(os.getenv('SUBSCRIPTION_MODEL_DIR', str(ROOT / 'final_subscription_model_v3'))).expanduser()
        )
        db.initialize()
        app.state.store = db
        yield

    app = FastAPI(title='Subscription Review API', version='1.0.0', lifespan=lifespan,
                  description='Local single-user V3 prototype. Predictions require user review; no automatic saving.')

    @app.exception_handler(MissingRecord)
    async def missing_handler(request, exc):
        return JSONResponse(status_code=404, content={'detail': str(exc)})

    @app.exception_handler(DecisionConflict)
    async def conflict_handler(request, exc):
        return JSONResponse(status_code=409, content={'detail': str(exc)})

    @app.get('/', include_in_schema=False)
    def home():
        return FileResponse(ROOT / 'static/index.html')

    @app.get('/health')
    def health(request: Request):
        return {'status': 'ready', 'model_version': request.app.state.model.version,
                'automatic_add_enabled': False, 'mode': 'local_single_user'}

    @app.post('/predict')
    def predict(body: PredictRequest, request: Request):
        try:
            prepared = preprocess_email(body.text)
        except ValueError as exc:
            raise HTTPException(status_code=422, detail=str(exc)) from exc
        scorer = request.app.state.model
        try:
            score = scorer.score(body.text)
            if not math.isfinite(score.probability) or not 0 <= score.probability <= 1:
                raise ValueError('Invalid model score')
        except Exception as exc:
            logger.error('Model inference failed (%s)', type(exc).__name__)
            raise HTTPException(status_code=503, detail='Prediction failed. Check the server/model setup and retry.') from exc
        sid = db.suggest(hashlib.sha256(prepared.encode('utf-8')).hexdigest(),
                         score.probability, scorer.threshold, scorer.version)
        return {'suggestion_id': sid,
                'predicted_label': 'Subscription' if score.probability >= scorer.threshold else 'Non-Subscription',
                'subscription_probability': score.probability, 'binary_threshold': scorer.threshold,
                'model_version': scorer.version, 'input_truncated': score.truncated,
                'extracted_details': extract_details(body.text) if score.probability >= scorer.threshold else {},
                'decision': 'REVIEW_REQUIRED', 'saved': False,
                'message': 'Review the full message. Confirm an active paid recurring plan and its details before saving.'}

    @app.post('/suggestions/{suggestion_id}/confirm')
    def confirm(suggestion_id: UUID, body: ConfirmRequest):
        details = body.model_dump(mode='json', exclude={'confirmed'})
        details['amount'] = format(body.amount.quantize(Decimal('0.01')), 'f')
        subscription, replayed = db.confirm(str(suggestion_id), details)
        return {'saved': True, 'decision': 'USER_CONFIRMED', 'already_saved': replayed, 'subscription': subscription}

    @app.post('/suggestions/{suggestion_id}/reject')
    def reject(suggestion_id: UUID):
        db.reject(str(suggestion_id))
        return {'saved': False, 'decision': 'USER_REJECTED', 'suggestion_id': str(suggestion_id)}

    @app.get('/subscriptions')
    def subscriptions():
        return {'items': db.subscriptions()}

    @app.put('/subscriptions/{subscription_id}')
    def update_subscription(subscription_id: UUID, body: ConfirmRequest):
        details = body.model_dump(mode='json', exclude={'confirmed'})
        details['amount'] = format(body.amount.quantize(Decimal('0.01')), 'f')
        return {'subscription': db.update(str(subscription_id), details)}

    @app.delete('/subscriptions/{subscription_id}')
    def delete_subscription(subscription_id: UUID):
        db.delete(str(subscription_id))
        return {'deleted': True}

    @app.get('/upcoming')
    def upcoming():
        today = today_provider() if today_provider else datetime.now(ZoneInfo('Asia/Colombo')).date()
        end = today + timedelta(days=6)
        items = []
        missing = overdue = 0
        for record in db.subscriptions():
            raw = record.get('next_billing_date')
            if not raw:
                missing += 1
                continue
            due = date.fromisoformat(raw)
            if due < today:
                overdue += 1
            elif due <= end:
                items.append({**record, 'days_until': (due - today).days,
                              'conversion': fx.convert(Decimal(record['amount']), record['currency'])})
        items.sort(key=lambda item: (item['next_billing_date'], item['service'].lower(), item['id']))
        return {'today': today.isoformat(), 'through': end.isoformat(), 'timezone': 'Asia/Colombo',
                'items': items, 'missing_date_count': missing, 'past_date_count': overdue}

    @app.get('/summary')
    def summary():
        records = db.subscriptions()
        total = Decimal('0')
        excluded = []
        stale_count = 0
        rate_dates = set()
        factors = {'monthly': Decimal('1'), 'weekly': Decimal('52') / 12,
                   'quarterly': Decimal('1') / 3, 'yearly': Decimal('1') / 12}
        for record in records:
            conversion = fx.convert(Decimal(record['amount']), record['currency'])
            if conversion['status'] == 'unavailable':
                excluded.append({'id': record['id'], 'service': record['service']})
                continue
            total += Decimal(record['amount']) * Decimal(conversion['rate']) * factors[record['billing_cycle']]
            stale_count += int(conversion['status'] == 'stale')
            if conversion.get('updated_at'):
                rate_dates.add(conversion['updated_at'])
        return {'subscription_count': len(records), 'included_count': len(records) - len(excluded),
                'monthly_lkr_estimate': str(total.quantize(Decimal('0.01'), rounding=ROUND_HALF_UP)),
                'is_partial': bool(excluded), 'excluded': excluded, 'stale_rate_count': stale_count,
                'rate_updated_at': sorted(rate_dates)}

    @app.post('/convert')
    def convert(body: ConversionRequest):
        return fx.convert(body.amount, body.currency)

    return app


app = create_app()
