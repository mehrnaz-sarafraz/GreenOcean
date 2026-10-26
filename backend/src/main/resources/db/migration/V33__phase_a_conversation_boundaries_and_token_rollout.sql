ALTER TABLE conversations ADD COLUMN service_kind VARCHAR(40) NOT NULL DEFAULT 'COMMUNITY'
    CHECK(service_kind IN ('COMMUNITY','LISTENER','PROFESSIONAL_CARE'));
UPDATE conversations SET service_kind='PROFESSIONAL_CARE' WHERE conversation_kind='PROFESSIONAL';
UPDATE conversations SET service_kind='LISTENER' WHERE conversation_kind='DIRECT' AND subtitle='Peer listener conversation';
-- Retire old unkeyed short codes and plaintext delivery payloads. Users can request a fresh code.
UPDATE email_verification_codes SET used_at=CURRENT_TIMESTAMP WHERE used_at IS NULL;
UPDATE password_reset_tokens SET used_at=CURRENT_TIMESTAMP WHERE used_at IS NULL;
UPDATE email_outbox SET payload='{}',status='FAILED' WHERE NOT jsonb_exists(payload,'encrypted');
