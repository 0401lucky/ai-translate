ALTER TABLE users ADD COLUMN email TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users(email)
  WHERE email IS NOT NULL;

CREATE TABLE IF NOT EXISTS email_verification_codes (
  id TEXT PRIMARY KEY,
  email TEXT NOT NULL,
  purpose TEXT NOT NULL,
  code_hash TEXT NOT NULL,
  created_at INTEGER NOT NULL,
  expires_at INTEGER NOT NULL,
  consumed_at INTEGER,
  attempts INTEGER NOT NULL DEFAULT 0,
  resend_message_id TEXT,
  request_ip TEXT
);

CREATE INDEX IF NOT EXISTS idx_email_verification_lookup
  ON email_verification_codes(email, purpose, created_at DESC);
