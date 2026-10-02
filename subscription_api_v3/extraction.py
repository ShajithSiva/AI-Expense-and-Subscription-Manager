"""Conservative, local suggestions. Missing or conflicting evidence stays empty."""
import re
from decimal import Decimal
from datetime import date

CURRENCIES = 'LKR USD EUR GBP INR AUD CAD SGD JPY AED'.split()

def extract_details(text):
    text = re.sub(r'<[^>]+>', ' ', text)
    text = re.sub(r'\s+', ' ', text).strip()
    result = dict.fromkeys(['service', 'amount', 'currency', 'billing_cycle', 'next_billing_date'])
    # Known names only: never guess a merchant from arbitrary sentence fragments.
    services = []
    for pattern, name in [(r'Spotify Premium', 'Spotify Premium'), (r'Netflix', 'Netflix'),
                          (r'Microsoft 365', 'Microsoft 365'), (r'Adobe Creative Cloud', 'Adobe Creative Cloud'),
                          (r'YouTube Premium', 'YouTube Premium'), (r'Apple Music', 'Apple Music'),
                          (r'Amazon Prime', 'Amazon Prime'), (r'iCloud\+?', 'iCloud')]:
        if re.search(r'\b' + pattern + r'\b', text, re.I):
            services.append(name)
    if len(services) == 1:
        result['service'] = services[0]
    # Multiple monetary mentions may represent discounts, taxes or future prices.
    money = re.findall(r'\b(' + '|'.join(CURRENCIES) + r')\s*((?:\d{1,3}(?:,\d{3})+|\d+)(?:\.\d{1,2})?)(?!\d|[.,]\d)', text, re.I)
    if len(money) == 1:
        currency, amount = money[0]
        value = Decimal(amount.replace(',', ''))
        if 0 < value <= Decimal('9999999999.99'):
            result['amount'] = format(value, '.2f')
            result['currency'] = currency.upper()
    cycles = []
    for cycle, pattern in [('weekly', r'weekly|every week|per week'),
                           ('monthly', r'monthly|every month|per month'),
                           ('quarterly', r'quarterly|every (?:3|three) months'),
                           ('yearly', r'yearly|annually|annual|every year|per year')]:
        if re.search(r'\b(?:' + pattern + r')\b', text, re.I):
            cycles.append(cycle)
    if len(cycles) == 1:
        result['billing_cycle'] = cycles[0]
    dates = re.findall(r'next (?:billing|payment|renewal) date\s*(?:is|:)?\s*(\d{4}-\d{2}-\d{2})\b', text, re.I)
    if len(dates) == 1:
        try:
            result['next_billing_date'] = date.fromisoformat(dates[0]).isoformat()
        except ValueError:
            pass
    return result
