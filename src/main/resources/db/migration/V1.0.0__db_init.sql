-- @formatter:off
CREATE TABLE IF NOT EXISTS mumu_comments (
                                             id INTEGER PRIMARY KEY AUTOINCREMENT,
                                             relative_path TEXT NOT NULL UNIQUE,
                                             comment TEXT
);

