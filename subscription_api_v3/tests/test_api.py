import concurrent.futures
import json
import sqlite3
import sys
from pathlib import Path
from uuid import uuid4

import pytest
from fastapi.testclient import TestClient

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from main import create_app
from model_service import Score, read_configuration
from preprocessing import preprocess_email


class FakeModel:
    version = 'v3'
    threshold = 0.5

    def score(self, text):
        if text == 'INVALID_SCORE':
            return Score(float('nan'), False)
        return Score(0.99 if 'renew' in text else 0.01, len(text) > 1500)


@pytest.fixture
def client(tmp_path):
    with TestClient(create_app(model=FakeModel(), database_path=tmp_path/'test.sqlite3')) as c:
        yield c


def analyze(client, text='Your paid plan will renew monthly.'):
    response = client.post('/predict', json={'text': text})
    assert response.status_code == 200
    return response.json()


def details(**changes):
    return {'confirmed': True, 'service': 'Example plan', 'amount': '12.50', 'currency': 'USD',
            'billing_cycle': 'monthly', 'next_billing_date': None, **changes}


def test_prediction_never_auto_saves(client):
    result = analyze(client)
    assert result['decision'] == 'REVIEW_REQUIRED' and result['saved'] is False
    assert result['predicted_label'] == 'Subscription'
    assert client.get('/subscriptions').json() == {'items': []}
    assert client.get('/health').json()['automatic_add_enabled'] is False


@pytest.mark.parametrize('value', [False, 'true', 1, None])
def test_explicit_boolean_confirmation_required(client, value):
    sid = analyze(client)['suggestion_id']
    response = client.post(f'/suggestions/{sid}/confirm', json=details(confirmed=value))
    assert response.status_code == 422
    assert not client.get('/subscriptions').json()['items']


def test_missing_confirmation_rejected(client):
    sid = analyze(client)['suggestion_id']; body = details(); body.pop('confirmed')
    assert client.post(f'/suggestions/{sid}/confirm', json=body).status_code == 422


@pytest.mark.parametrize('change', [{'amount':'NaN'},{'amount':'Infinity'},{'amount':'-1'},{'amount':'0'},
                                   {'amount':'1.234'},{'service':'  '},{'currency':'usd'},
                                   {'billing_cycle':'one-time'},{'next_billing_date':'not-a-date'}])
def test_invalid_details_cannot_save(client, change):
    sid = analyze(client)['suggestion_id']
    assert client.post(f'/suggestions/{sid}/confirm', json=details(**change)).status_code == 422
    assert not client.get('/subscriptions').json()['items']


def test_user_can_correct_false_negative_and_retries_are_idempotent(client):
    result = analyze(client, 'My membership is paid every month.')
    assert result['predicted_label'] == 'Non-Subscription'
    url = f"/suggestions/{result['suggestion_id']}/confirm"
    first = client.post(url, json=details()).json()
    retry = client.post(url, json=details(amount='12.5')).json()
    assert first['decision'] == 'USER_CONFIRMED' and first['already_saved'] is False
    assert retry['already_saved'] is True and retry['subscription']['id'] == first['subscription']['id']
    assert len(client.get('/subscriptions').json()['items']) == 1
    assert client.post(url, json=details(amount='20')).status_code == 409


def test_reanalyzing_same_message_cannot_duplicate_subscription(client):
    first = analyze(client)['suggestion_id']
    assert client.post(f'/suggestions/{first}/confirm', json=details()).status_code == 200
    second = analyze(client)['suggestion_id']
    assert client.post(f'/suggestions/{second}/confirm', json=details()).status_code == 409
    assert len(client.get('/subscriptions').json()['items']) == 1


def test_reject_flow_and_invalid_state_transition(client):
    sid = analyze(client)['suggestion_id']
    assert client.post(f'/suggestions/{sid}/reject').status_code == 200
    assert client.post(f'/suggestions/{sid}/reject').status_code == 200
    assert client.post(f'/suggestions/{sid}/confirm', json=details()).status_code == 409
    sid2 = analyze(client, 'Another plan will renew.')['suggestion_id']
    client.post(f'/suggestions/{sid2}/confirm', json=details())
    assert client.post(f'/suggestions/{sid2}/reject').status_code == 409


def test_missing_ids(client):
    sid = uuid4()
    assert client.post(f'/suggestions/{sid}/confirm', json=details()).status_code == 404
    assert client.post(f'/suggestions/{sid}/reject').status_code == 404


@pytest.mark.parametrize('text', ['', '   ', '<p></p>', 'https://example.com', 'x'*20001])
def test_empty_and_oversized_messages_rejected(client, text):
    assert client.post('/predict', json={'text': text}).status_code == 422


def test_invalid_model_score_does_not_save(client):
    assert client.post('/predict', json={'text':'INVALID_SCORE'}).status_code == 503
    assert not client.get('/subscriptions').json()['items']


def test_truncation_is_reported(client):
    assert analyze(client, 'renew ' * 300)['input_truncated'] is True


def test_concurrent_confirmations_create_one_subscription(client):
    sid = analyze(client)['suggestion_id']
    with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:
        responses = list(pool.map(lambda _: client.post(f'/suggestions/{sid}/confirm', json=details()), range(6)))
    assert all(r.status_code == 200 for r in responses)
    assert len({r.json()['subscription']['id'] for r in responses}) == 1
    assert len(client.get('/subscriptions').json()['items']) == 1


def test_persistence_and_no_raw_message_storage(tmp_path):
    db = tmp_path/'persist.sqlite3'; raw='Private sender: paid plan will renew for me.'
    with TestClient(create_app(model=FakeModel(), database_path=db)) as c:
        sid = analyze(c, raw)['suggestion_id']
        c.post(f'/suggestions/{sid}/confirm', json=details())
    with TestClient(create_app(model=FakeModel(), database_path=db)) as c:
        assert len(c.get('/subscriptions').json()['items']) == 1
    with sqlite3.connect(db) as conn:
        assert raw not in '\n'.join(conn.iterdump())


def test_ui_and_docs_available(client):
    assert client.get('/').status_code == 200
    assert 'Confirm and save' in client.get('/').text
    assert client.get('/docs').status_code == 200
    assert '/suggestions/{suggestion_id}/confirm' in client.get('/openapi.json').json()['paths']


def test_preprocessing_matches_training_contract():
    assert preprocess_email('<p>Your plan renews.</p>\n https://example.com a@b.com') == 'Your plan renews.'


def config_files(path, **overrides):
    config=dict(dataset_version='v3',subscription_label_id=1,vendor_masking=False,text_weight=1.0,
                recurrence_weight=0.0,binary_threshold=0.5,max_length=256,**overrides)
    (path/'configuration.json').write_text(json.dumps(config))
    (path/'preprocessing_config.json').write_text(json.dumps(dict(subscription_label_id=1,vendor_masking=False,max_length=256)))
    return config


def test_missing_export_fails_clearly(tmp_path):
    with pytest.raises(RuntimeError, match='SUBSCRIPTION_MODEL_DIR'):
        read_configuration(tmp_path)


@pytest.mark.parametrize('field,value', [('subscription_label_id',0),('vendor_masking',True),
                                       ('binary_threshold',float('nan')),('binary_threshold',True),
                                       ('recurrence_weight',0.2),('max_length',150),('dataset_version','v2')])
def test_incompatible_configuration_is_rejected(tmp_path, field, value):
    conf=config_files(tmp_path); conf[field]=value
    (tmp_path/'configuration.json').write_text(json.dumps(conf))
    with pytest.raises(RuntimeError):
        read_configuration(tmp_path)


def test_valid_v3_config(tmp_path):
    config_files(tmp_path)
    assert read_configuration(tmp_path)['binary_threshold'] == .5
