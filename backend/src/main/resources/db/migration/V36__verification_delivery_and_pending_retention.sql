-- Additive D rollout. Preserve users, code lifetime, existing ciphertext and accepted policies.
ALTER TABLE users ADD COLUMN verification_activity_at TIMESTAMPTZ;
UPDATE users SET verification_activity_at=GREATEST(created_at,COALESCE(last_login_at,created_at)) WHERE status='PENDING';

-- Retain one current challenge before adding the invariant; older challenges require resend.
WITH ranked AS (
 SELECT id,ROW_NUMBER() OVER(PARTITION BY user_id ORDER BY created_at DESC,id DESC) AS n
 FROM email_verification_codes WHERE used_at IS NULL
) UPDATE email_verification_codes SET used_at=CURRENT_TIMESTAMP WHERE id IN (SELECT id FROM ranked WHERE n>1);
CREATE UNIQUE INDEX uq_verification_current_challenge ON email_verification_codes(user_id) WHERE used_at IS NULL;

ALTER TABLE email_outbox DROP CONSTRAINT chk_email_outbox_status;
ALTER TABLE email_outbox ADD CONSTRAINT chk_email_outbox_status CHECK(status IN ('PENDING','SENDING','SENT','FAILED','DEAD','CANCELLED'));
ALTER TABLE email_outbox ADD COLUMN owner_user_id UUID REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE email_outbox ADD COLUMN verification_challenge_id UUID REFERENCES email_verification_codes(id) ON DELETE SET NULL;
ALTER TABLE email_outbox ADD COLUMN next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE email_outbox ADD COLUMN lease_token UUID;
ALTER TABLE email_outbox ADD COLUMN lease_until TIMESTAMPTZ;
ALTER TABLE email_outbox ADD COLUMN provider_reference VARCHAR(160);
ALTER TABLE email_outbox ADD COLUMN outcome_code VARCHAR(40);
ALTER TABLE email_outbox ADD COLUMN delivery_state VARCHAR(20) CHECK(delivery_state IN ('ACCEPTED','DELIVERED','BOUNCED'));
ALTER TABLE email_outbox ADD COLUMN delivery_event_at TIMESTAMPTZ;
UPDATE email_outbox o SET owner_user_id=u.id FROM users u WHERE lower(o.recipient)=lower(u.email);
UPDATE email_outbox SET delivery_state='ACCEPTED' WHERE status='SENT' AND sent_at IS NOT NULL;
UPDATE email_outbox o SET verification_challenge_id=c.id FROM email_verification_codes c
 WHERE o.owner_user_id=c.user_id AND c.used_at IS NULL AND o.template_key='email-verification';
UPDATE email_outbox SET status='DEAD',payload='{}',outcome_code='RETRY_EXHAUSTED' WHERE status='FAILED' AND attempts>=5;
CREATE INDEX idx_outbox_due ON email_outbox(next_attempt_at) WHERE status IN ('PENDING','FAILED','SENDING');
CREATE INDEX idx_outbox_owner ON email_outbox(owner_user_id);
CREATE INDEX idx_pending_retention ON users(verification_activity_at) WHERE status='PENDING' AND NOT email_verified AND phase_a_enrolled;
