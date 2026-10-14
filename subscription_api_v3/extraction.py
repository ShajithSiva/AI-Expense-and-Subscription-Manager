
"""
Conservative subscription information extraction.

Extracts:
- Service name
- Subscription amount
- Currency
- Billing cycle
- Next billing date

Ambiguous or missing information stays None.
"""

import re
from datetime import date, datetime
from decimal import Decimal, InvalidOperation


# Supported currencies
CURRENCIES = {
    "LKR", "USD", "EUR", "GBP", "INR",
    "AUD", "CAD", "SGD", "JPY", "AED",
}


# Recognized service names
SERVICE_PATTERNS = [
    (r"\bSpotify(?:\s+Premium)?\b", "Spotify Premium"),
    (r"\bNetflix\b", "Netflix"),
    (r"\bMicrosoft\s+365\b", "Microsoft 365"),
    (r"\bAdobe\s+Creative\s+Cloud\b", "Adobe Creative Cloud"),
    (r"\bYouTube\s+Premium\b", "YouTube Premium"),
    (r"\bApple\s+Music\b", "Apple Music"),
    (r"\bAmazon\s+Prime\s+Video\b", "Amazon Prime Video"),
    (r"\bAmazon\s+Prime\b", "Amazon Prime"),
    (r"\biCloud(?:\s*\+)?\b", "iCloud"),
    (r"\bKandy\s+Fitness\s+Club\b", "Kandy Fitness Club"),
    (r"\bDisney\s*\+\b", "Disney+"),
    (r"\bGoogle\s+One\b", "Google One"),
    (r"\bChatGPT\s+Plus\b", "ChatGPT Plus"),
    (r"\bOpenAI\b", "OpenAI"),
    (r"\bZoom\s+(?:Pro|Business|One)\b", "Zoom"),
    (r"\bCanva\s+Pro\b", "Canva Pro"),
]


def extract_service(text: str):
    """Return a service only when exactly one service is recognized."""

    found = {
        name
        for pattern, name in SERVICE_PATTERNS
        if re.search(pattern, text, re.IGNORECASE)
    }

    return next(iter(found)) if len(found) == 1 else None


def _parse_amount(raw_amount: str):
    """Parse and validate a positive monetary amount."""

    try:
        amount = Decimal(raw_amount.replace(",", "").strip())
    except (InvalidOperation, ValueError):
        return None

    if not amount.is_finite():
        return None

    if not (Decimal("0") < amount <= Decimal("9999999999.99")):
        return None

    return format(amount, ".2f")


def extract_amount_and_currency(text: str):
    """
    Supports:
        USD 15.49
        LKR 1,500.00
        Rs. 1,500
        $15.49
        US$15.49
        15.49 USD
        1500 LKR

    If multiple distinct monetary values are found, return None, None.
    """

    amount_pattern = (
        r"(?:\d{1,3}(?:,\d{3})+|\d+)"
        r"(?:\.\d{1,2})?"
    )

    currency_pattern = (
        r"US\$|CA\$|AU\$|S\$|"
        r"LKR|USD|EUR|GBP|INR|AUD|CAD|SGD|JPY|AED|"
        r"Rs\.?|€|£|¥|\$"
    )

    currency_map = {
        "US$": "USD",
        "CA$": "CAD",
        "AU$": "AUD",
        "S$": "SGD",
        "LKR": "LKR",
        "USD": "USD",
        "EUR": "EUR",
        "GBP": "GBP",
        "INR": "INR",
        "AUD": "AUD",
        "CAD": "CAD",
        "SGD": "SGD",
        "JPY": "JPY",
        "AED": "AED",
        "RS": "LKR",
        "RS.": "LKR",
        "$": "USD",
        "€": "EUR",
        "£": "GBP",
        "¥": "JPY",
    }

    # Currency before amount OR amount before currency.
    pattern = re.compile(
        rf"(?<![\w.])"
        rf"({currency_pattern})\s*"
        rf"({amount_pattern})(?![\d.,])"
        rf"|"
        rf"(?<![\d.,])"
        rf"({amount_pattern})\s*"
        rf"({currency_pattern})(?!\w)",
        re.IGNORECASE,
    )

    candidates = set()

    for match in pattern.finditer(text):
        if match.group(1) is not None:
            raw_currency = match.group(1)
            raw_amount = match.group(2)
        else:
            raw_amount = match.group(3)
            raw_currency = match.group(4)

        currency = currency_map.get(raw_currency.upper())
        amount = _parse_amount(raw_amount)

        if amount is not None and currency in CURRENCIES:
            candidates.add((amount, currency))

    # A repeated identical amount is acceptable.
    # Different amounts or currencies are ambiguous.
    if len(candidates) != 1:
        return None, None

    amount, currency = next(iter(candidates))
    return amount, currency


def extract_billing_cycle(text: str):
    """Identify one clearly stated recurring billing interval."""

    patterns = [
        (
            "weekly",
            r"\b(?:weekly|every\s+week|per\s+week|"
            r"each\s+week|once\s+a\s+week)\b",
        ),
        (
            "monthly",
            r"\b(?:monthly|every\s+month|per\s+month|"
            r"each\s+month|once\s+a\s+month|billed\s+monthly)\b",
        ),
        (
            "quarterly",
            r"\b(?:quarterly|every\s+3\s+months|"
            r"every\s+three\s+months|once\s+a\s+quarter)\b",
        ),
        (
            "yearly",
            r"\b(?:yearly|annually|annual|every\s+year|"
            r"per\s+year|each\s+year|once\s+a\s+year|"
            r"billed\s+annually)\b",
        ),
    ]

    found = {
        cycle
        for cycle, pattern in patterns
        if re.search(pattern, text, re.IGNORECASE)
    }

    return next(iter(found)) if len(found) == 1 else None


def extract_next_billing_date(text: str):
    """
    Extract a date explicitly associated with billing.

    Examples:
        Next automatic billing date will be November 5, 2026.
        Next payment is due on Nov 5, 2026.
        Renews on November 5, 2026.
        Renewal date: 2026-11-05.
    """

    date_value = (
        r"(?:"
        r"[A-Za-z]{3,9}\s+\d{1,2},?\s+\d{4}"
        r"|"
        r"\d{4}-\d{2}-\d{2}"
        r")"
    )

    date_pattern = re.compile(
        rf"\b(?:"
        rf"next\s+(?:(?:automatic|scheduled)\s+)?"
        rf"(?:billing|payment|renewal)(?:\s+date)?"
        rf"|next\s+charge(?:\s+date)?"
        rf"|renews\s+on"
        rf"|renewal\s+date"
        rf")"
        rf"\s*"
        rf"(?:(?:will\s+be|is|will\s+occur|occurs)\s+)?"
        rf"(?:(?:scheduled\s+)?for\s+|on\s+|:\s*)?"
        rf"({date_value})",
        re.IGNORECASE,
    )

    matches = list(date_pattern.finditer(text))

    # Do not guess if multiple possible billing dates exist.
    if len(matches) != 1:
        return None

    raw_date = matches[0].group(1).strip()

    formats = [
        "%B %d, %Y",
        "%B %d %Y",
        "%b %d, %Y",
        "%b %d %Y",
        "%Y-%m-%d",
    ]

    for fmt in formats:
        try:
            parsed = datetime.strptime(raw_date, fmt).date()

            # Ignore past billing dates.
            if parsed < date.today():
                return None

            return parsed.isoformat()

        except ValueError:
            continue

    return None


def extract_details(text: str):
    """Return extracted fields using the existing API field names."""

    empty_result = {
        "service": None,
        "amount": None,
        "currency": None,
        "billing_cycle": None,
        "next_billing_date": None,
    }

    if not isinstance(text, str) or not text.strip():
        return empty_result

    # Remove HTML tags and normalize whitespace.
    text = re.sub(r"<[^>]*>", " ", text)
    text = re.sub(
        r"&nbsp;|&#160;",
        " ",
        text,
        flags=re.IGNORECASE,
    )
    text = re.sub(r"\s+", " ", text).strip()

    amount, currency = extract_amount_and_currency(text)

    return {
        "service": extract_service(text),
        "amount": amount,
        "currency": currency,
        "billing_cycle": extract_billing_cycle(text),
        "next_billing_date": extract_next_billing_date(text),
    }
