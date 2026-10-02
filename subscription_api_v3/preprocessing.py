"""Identical text preparation to Train_Subscription_V3.ipynb."""
import re


def preprocess_email(text):
    if not isinstance(text, str) or not text.strip():
        raise ValueError('A non-empty message string is required.')
    text = re.sub(r'<[^>]+>', ' ', text)
    text = re.sub(r'http\S+|www\.\S+', ' ', text)
    text = re.sub(r'\S+@\S+', ' ', text)
    text = re.sub(r'\s+', ' ', text).strip()
    if not text:
        raise ValueError('No message content remains after cleaning.')
    return text
