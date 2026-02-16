-- Create Virtual Table mirroring summaryJson for FTS
CREATE VIRTUAL TABLE artifacts_fts USING fts5(
    summaryJson,
    content='artifacts',
    content_rowid='id'
);

-- Triggers to keep FTS index in sync
CREATE TRIGGER artifacts_ai AFTER INSERT ON artifacts BEGIN
  INSERT INTO artifacts_fts(rowid, summaryJson) VALUES (new.id, new.summaryJson);
END;

CREATE TRIGGER artifacts_ad AFTER DELETE ON artifacts BEGIN
  INSERT INTO artifacts_fts(artifacts_fts, rowid, summaryJson) VALUES('delete', old.id, old.summaryJson);
END;

CREATE TRIGGER artifacts_au AFTER UPDATE ON artifacts BEGIN
  INSERT INTO artifacts_fts(artifacts_fts, rowid, summaryJson) VALUES('delete', old.id, old.summaryJson);
  INSERT INTO artifacts_fts(rowid, summaryJson) VALUES (new.id, new.summaryJson);
END;

-- Rebuild index for existing data
INSERT INTO artifacts_fts(artifacts_fts) VALUES('rebuild');
