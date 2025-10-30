-- =====================================================
-- Soccer Fixtures App - Complete Database Schema
-- =====================================================

-- Leagues Table
CREATE TABLE IF NOT EXISTS leagues (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    country VARCHAR(100),
    country_code VARCHAR(5),
    logo_url TEXT
);

-- Teams Table
CREATE TABLE IF NOT EXISTS teams (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    short_name VARCHAR(100),
    logo_url TEXT,
    league_id INT REFERENCES leagues(id),
    updated_at TIMESTAMP
);

-- Fixtures Table
CREATE TABLE IF NOT EXISTS fixtures (
    id BIGINT PRIMARY KEY,
    league_id INT NOT NULL,
    home_team_id INT NOT NULL,
    away_team_id INT NOT NULL,
    home_score INT,
    away_score INT,
    match_utc TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_fixtures_league
        FOREIGN KEY (league_id) REFERENCES leagues(id)
        ON UPDATE CASCADE ON DELETE CASCADE,

    CONSTRAINT fk_fixtures_home_team
        FOREIGN KEY (home_team_id) REFERENCES teams(id)
        ON UPDATE CASCADE ON DELETE CASCADE,

    CONSTRAINT fk_fixtures_away_team
        FOREIGN KEY (away_team_id) REFERENCES teams(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- Index for fixtures by match time
CREATE INDEX IF NOT EXISTS idx_fixtures_match_utc ON fixtures(match_utc);

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(320) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    timezone VARCHAR(64) NOT NULL DEFAULT 'UTC',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Notification Subscriptions Table
CREATE TABLE IF NOT EXISTS notification_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    channel TEXT NOT NULL CHECK (channel IN ('EMAIL','SMS')),
    target_type TEXT NOT NULL CHECK (target_type IN ('TEAM','LEAGUE')),
    target_id INT NOT NULL,
    minutes_before_kickoff INT NOT NULL DEFAULT 60,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE (user_id, channel, target_type, target_id)
);

-- Notification Receipts Table (idempotency)
CREATE TABLE IF NOT EXISTS notification_receipts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fixture_id BIGINT NOT NULL REFERENCES fixtures(id) ON DELETE CASCADE,
    kind TEXT NOT NULL CHECK (kind IN ('PREMATCH','FINAL')),
    channel TEXT NOT NULL CHECK (channel IN ('EMAIL','SMS')),
    sent_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE (user_id, fixture_id, kind, channel)
);

-- Indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_sub_user_active
    ON notification_subscriptions(user_id, active);

CREATE INDEX IF NOT EXISTS idx_sub_target
    ON notification_subscriptions(target_type, target_id, active);

CREATE INDEX IF NOT EXISTS idx_receipts_user_fix
    ON notification_receipts(user_id, fixture_id);