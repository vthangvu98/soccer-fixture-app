CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(320) UNIQUE,               -- nullable if SMS-only later; else enforce NOT NULL
  phone VARCHAR(20) UNIQUE,                -- for SMS later (E.164)
  tz VARCHAR(64) NOT NULL DEFAULT 'UTC',   -- IANA tz
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Who wants what, how, and when
CREATE TABLE IF NOT EXISTS notification_subscriptions (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  channel TEXT NOT NULL CHECK (channel IN ('EMAIL','SMS')),            -- start with EMAIL only
  target_type TEXT NOT NULL CHECK (target_type IN ('TEAM','LEAGUE')),
  target_id INT NOT NULL,                                              -- team.id or league.id
  minutes_before_kickoff INT NOT NULL DEFAULT 60,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

  -- prevent exact duplicates per user
  UNIQUE (user_id, channel, target_type, target_id)
);

-- Idempotency: exactly-once per user+fixture+kind+channel
CREATE TABLE IF NOT EXISTS notification_receipts (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  fixture_id BIGINT NOT NULL REFERENCES fixtures(id) ON DELETE CASCADE,
  kind TEXT NOT NULL CHECK (kind IN ('PREMATCH','FINAL')),
  channel TEXT NOT NULL CHECK (channel IN ('EMAIL','SMS')),
  sent_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

  UNIQUE (user_id, fixture_id, kind, channel)
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_sub_user_active ON notification_subscriptions(user_id, active);
CREATE INDEX IF NOT EXISTS idx_sub_target ON notification_subscriptions(target_type, target_id, active);
CREATE INDEX IF NOT EXISTS idx_receipts_user_fix ON notification_receipts(user_id, fixture_id);
