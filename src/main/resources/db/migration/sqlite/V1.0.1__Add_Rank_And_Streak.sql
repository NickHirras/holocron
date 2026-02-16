-- V1.0.1__Add_Rank_And_Streak.sql
-- Adds support for Council of Ranks mechanics: XP, Streaks, and Ranks

CREATE TABLE ranks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    minXp BIGINT NOT NULL,
    badgeUrl VARCHAR(255)
);

CREATE TABLE user_stats (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL UNIQUE,
    currentStreak INTEGER DEFAULT 0,
    longestStreak INTEGER DEFAULT 0,
    lastPulseDate DATE,
    totalXp BIGINT DEFAULT 0,
    FOREIGN KEY(user_id) REFERENCES users(id)
);

-- Index for leaderboard queries on XP
CREATE INDEX idx_user_stats_xp ON user_stats(totalXp DESC);

-- Initial Rank Data (Optional, but good for reference)
INSERT INTO ranks (name, minXp, badgeUrl) VALUES ('Padawan', 0, '/images/badges/padawan.png');
INSERT INTO ranks (name, minXp, badgeUrl) VALUES ('Knight', 1000, '/images/badges/knight.png');
INSERT INTO ranks (name, minXp, badgeUrl) VALUES ('Master', 5000, '/images/badges/master.png');
