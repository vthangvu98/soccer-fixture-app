-- Leagues (API league id as PK)
CREATE TABLE IF NOT EXISTS leagues (
  id INT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  country VARCHAR(100),
  country_code VARCHAR(5),
  logo_url TEXT
);

-- Teams (API team id as PK)
CREATE TABLE IF NOT EXISTS teams (
  id INT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  short_name VARCHAR(100),
  logo_url TEXT,
  league_id INT REFERENCES leagues(id),
  updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fixtures (
    id BIGINT PRIMARY KEY,               -- match id from API
    league_id INT NOT NULL ,              -- FK to leagues(id)
    home_team_id INT NOT NULL,           -- FK to teams(id)
    away_team_id INT NOT NULL,           -- FK to teams(id)
    home_score INT,                      -- nullable until game finished
    away_score INT,                      -- nullable until game finished
    match_utc TIMESTAMPTZ,               -- kickoff time in UTC
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_fixtures_league FOREIGN KEY (league_id)
        REFERENCES leagues(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_fixtures_home_team FOREIGN KEY (home_team_id)
        REFERENCES teams(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_fixtures_away_team FOREIGN KEY (away_team_id)
        REFERENCES teams(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
