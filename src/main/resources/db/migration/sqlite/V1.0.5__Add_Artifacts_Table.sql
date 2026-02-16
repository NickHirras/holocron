CREATE TABLE artifacts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    team_id INTEGER,
    ceremony_id INTEGER,
    periodStart DATE,
    periodEnd DATE,
    summaryJson TEXT,
    createdAt TIMESTAMP,
    FOREIGN KEY(team_id) REFERENCES teams(id),
    FOREIGN KEY(ceremony_id) REFERENCES ceremonies(id)
);

CREATE INDEX idx_artifact_team_period ON artifacts(team_id, periodStart);
