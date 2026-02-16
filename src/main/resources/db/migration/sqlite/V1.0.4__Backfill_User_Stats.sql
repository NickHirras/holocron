-- V1.0.4__Backfill_User_Stats.sql
-- Ensures every user has a corresponding user_stats record.

INSERT INTO user_stats (user_id, currentStreak, longestStreak, totalXp)
SELECT id, 0, 0, 0
FROM users
WHERE id NOT IN (SELECT user_id FROM user_stats);
