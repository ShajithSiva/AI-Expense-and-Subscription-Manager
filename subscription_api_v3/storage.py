"""Single-user local SQLite storage. Prediction does not create a subscription."""
import json
import sqlite3
from contextlib import contextmanager
from datetime import datetime, timezone
from pathlib import Path
from uuid import uuid4


class MissingRecord(Exception):
    pass


class DecisionConflict(Exception):
    pass


def now():
    return datetime.now(timezone.utc).isoformat()


class Store:
    def __init__(self, path: Path):
        self.path = path

    @contextmanager
    def connection(self, write=False):
        conn = sqlite3.connect(self.path, timeout=15, isolation_level=None)
        conn.row_factory = sqlite3.Row
        conn.execute('PRAGMA foreign_keys=ON')
        try:
            if write:
                conn.execute('BEGIN IMMEDIATE')
            yield conn
            if write:
                conn.commit()
        except Exception:
            if write:
                conn.rollback()
            raise
        finally:
            conn.close()

    def initialize(self):
        self.path.parent.mkdir(parents=True, exist_ok=True)
        with self.connection() as c:
            c.execute('PRAGMA journal_mode=WAL')
            c.executescript('''
                CREATE TABLE IF NOT EXISTS suggestions (
                    id TEXT PRIMARY KEY, source_hash TEXT NOT NULL,
                    probability REAL NOT NULL CHECK(probability BETWEEN 0 AND 1),
                    threshold REAL NOT NULL, model_version TEXT NOT NULL,
                    status TEXT NOT NULL CHECK(status IN ('pending','confirmed','rejected')),
                    created_at TEXT NOT NULL
                );
                CREATE TABLE IF NOT EXISTS subscriptions (
                    id TEXT PRIMARY KEY,
                    suggestion_id TEXT NOT NULL UNIQUE REFERENCES suggestions(id),
                    source_hash TEXT NOT NULL UNIQUE,
                    details_json TEXT NOT NULL,
                    created_at TEXT NOT NULL
                );
            ''')

    def suggest(self, source_hash, probability, threshold, model_version):
        sid = str(uuid4())
        with self.connection(write=True) as c:
            c.execute('INSERT INTO suggestions VALUES (?,?,?,?,?,?,?)',
                      (sid, source_hash, probability, threshold, model_version, 'pending', now()))
        return sid

    @staticmethod
    def unpack(row):
        return {'id': row['id'], 'suggestion_id': row['suggestion_id'],
                **json.loads(row['details_json']), 'created_at': row['created_at']}

    def confirm(self, sid, details):
        payload = json.dumps(details, sort_keys=True, ensure_ascii=False)
        with self.connection(write=True) as c:
            suggestion = c.execute('SELECT * FROM suggestions WHERE id=?', (sid,)).fetchone()
            if suggestion is None:
                raise MissingRecord('Suggestion not found. Analyze the message first.')
            if suggestion['status'] == 'rejected':
                raise DecisionConflict('This suggestion was rejected. Analyze the message again to reconsider it.')
            existing = c.execute('SELECT * FROM subscriptions WHERE suggestion_id=?', (sid,)).fetchone()
            if existing:
                if existing['details_json'] != payload:
                    raise DecisionConflict('This suggestion was already confirmed with different details.')
                return self.unpack(existing), True
            duplicate = c.execute('SELECT id FROM subscriptions WHERE source_hash=?', (suggestion['source_hash'],)).fetchone()
            if duplicate:
                raise DecisionConflict('This message already has a saved subscription. Check your saved list.')
            subscription_id = str(uuid4())
            created_at = now()
            c.execute('INSERT INTO subscriptions VALUES (?,?,?,?,?)',
                      (subscription_id, sid, suggestion['source_hash'], payload, created_at))
            c.execute("UPDATE suggestions SET status='confirmed' WHERE id=?", (sid,))
            row = c.execute('SELECT * FROM subscriptions WHERE id=?', (subscription_id,)).fetchone()
            return self.unpack(row), False

    def reject(self, sid):
        with self.connection(write=True) as c:
            row = c.execute('SELECT status FROM suggestions WHERE id=?', (sid,)).fetchone()
            if row is None:
                raise MissingRecord('Suggestion not found.')
            if row['status'] == 'confirmed':
                raise DecisionConflict('A confirmed subscription cannot be rejected through this endpoint.')
            c.execute("UPDATE suggestions SET status='rejected' WHERE id=?", (sid,))

    def subscriptions(self):
        with self.connection() as c:
            rows = c.execute('SELECT * FROM subscriptions ORDER BY created_at DESC').fetchall()
            return [self.unpack(row) for row in rows]

    def update(self, subscription_id, details):
        with self.connection(write=True) as c:
            row = c.execute('SELECT * FROM subscriptions WHERE id=?', (subscription_id,)).fetchone()
            if row is None:
                raise MissingRecord('Subscription not found.')
            c.execute('UPDATE subscriptions SET details_json=? WHERE id=?',
                      (json.dumps(details, sort_keys=True, ensure_ascii=False), subscription_id))
            return self.unpack(c.execute('SELECT * FROM subscriptions WHERE id=?', (subscription_id,)).fetchone())

    def delete(self, subscription_id):
        with self.connection(write=True) as c:
            row = c.execute('SELECT suggestion_id FROM subscriptions WHERE id=?', (subscription_id,)).fetchone()
            if row is None:
                raise MissingRecord('Subscription not found.')
            c.execute('DELETE FROM subscriptions WHERE id=?', (subscription_id,))
            # Prevent an old confirmation request from recreating the deleted record.
            c.execute("UPDATE suggestions SET status='rejected' WHERE id=?", (row['suggestion_id'],))
