"""Daily indicative rates; only a public USD rate table leaves this app."""
import json
import time
from datetime import datetime, timezone
from decimal import Decimal, ROUND_HALF_UP
from threading import Lock
from urllib.request import urlopen

URL = 'https://open.er-api.com/v6/latest/USD'


def fetch_rates():
    with urlopen(URL, timeout=5) as response:
        return json.loads(response.read(200000))


class CurrencyService:
    def __init__(self, path, fetch=fetch_rates, clock=time.time):
        self.path, self.fetch, self.clock = path, fetch, clock
        self.lock = Lock()
        self.data = None
        self.retry_at = 0
        try:
            self.data = self.validate(json.loads(path.read_text()))
        except (OSError, ValueError, TypeError, KeyError):
            pass

    def validate(self, data):
        if data['result'] != 'success' or data['base_code'] != 'USD':
            raise ValueError('Invalid rate response')
        stamp = data['time_last_update_unix']
        if isinstance(stamp, bool) or not isinstance(stamp, (int, float)) or not 0 < stamp <= self.clock() + 300:
            raise ValueError('Invalid rate timestamp')
        rates = data['rates']
        for code in ('USD', 'LKR'):
            value = Decimal(str(rates[code]))
            if not value.is_finite() or value <= 0:
                raise ValueError('Invalid rate')
        if Decimal(str(rates['USD'])) != 1:
            raise ValueError('Invalid base rate')
        return data

    def convert(self, amount, currency):
        if currency == 'LKR':
            return {'status': 'original', 'amount_lkr': format(amount, '.2f'), 'rate': '1', 'updated_at': None}
        with self.lock:
            now = self.clock()
            if (self.data is None or now - self.data['time_last_update_unix'] >= 86400) and now >= self.retry_at:
                self.retry_at = now + 3600
                try:
                    self.data = self.validate(self.fetch())
                    try:
                        self.path.parent.mkdir(parents=True, exist_ok=True)
                        temp = self.path.with_suffix('.tmp')
                        temp.write_text(json.dumps(self.data))
                        temp.replace(self.path)
                    except OSError:
                        pass  # A read-only cache must not prevent conversion.
                except Exception:
                    pass  # Never block saving when the provider is unavailable.
            data = self.data
            if data is None or now - data['time_last_update_unix'] > 7 * 86400:
                return {'status': 'unavailable'}
            try:
                base = Decimal(str(data['rates'][currency]))
                if not base.is_finite() or base <= 0:
                    raise ValueError('Invalid currency rate')
                rate = Decimal(str(data['rates']['LKR'])) / base
                converted = (amount * rate).quantize(Decimal('0.01'), rounding=ROUND_HALF_UP)
                return {'status': 'stale' if now - data['time_last_update_unix'] >= 86400 else 'available',
                        'amount_lkr': str(converted), 'rate': str(rate),
                        'updated_at': datetime.fromtimestamp(data['time_last_update_unix'], timezone.utc).isoformat()}
            except (KeyError, ValueError, ArithmeticError):
                return {'status': 'unavailable'}
