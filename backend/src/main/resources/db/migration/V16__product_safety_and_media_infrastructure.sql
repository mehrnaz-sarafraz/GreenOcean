ALTER TABLE media ALTER COLUMN post_id DROP NOT NULL;
ALTER TABLE media
    ADD COLUMN media_status VARCHAR(20) NOT NULL DEFAULT 'READY',
    ADD COLUMN purpose VARCHAR(30) NOT NULL DEFAULT 'POST',
    ADD COLUMN expires_at TIMESTAMPTZ NULL,
    ADD COLUMN deleted_at TIMESTAMPTZ NULL,
    ADD CONSTRAINT uq_media_storage_key UNIQUE (storage_key),
    ADD CONSTRAINT chk_media_status CHECK (media_status IN ('PENDING', 'READY', 'ATTACHED', 'REJECTED', 'DELETED')),
    ADD CONSTRAINT chk_media_purpose CHECK (purpose IN ('POST', 'VERIFICATION', 'PROFILE')),
    ADD CONSTRAINT chk_media_attachment CHECK (
        (purpose = 'POST' AND media_status = 'ATTACHED' AND post_id IS NOT NULL)
        OR NOT (purpose = 'POST' AND media_status = 'ATTACHED')
    );

UPDATE media SET media_status = 'ATTACHED' WHERE post_id IS NOT NULL;

CREATE INDEX idx_media_uploader_status ON media (uploader_id, media_status, created_at DESC);
CREATE INDEX idx_media_expiry ON media (expires_at) WHERE media_status = 'PENDING';

ALTER TABLE professional_verifications
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN submitted_profile JSONB NOT NULL DEFAULT '{}'::jsonb,
    ADD COLUMN info_request VARCHAR(1000) NULL;

CREATE UNIQUE INDEX uq_professional_verification_open
    ON professional_verifications (professional_user_id)
    WHERE status = 'PENDING';

CREATE TABLE professional_verification_documents (
    verification_id UUID NOT NULL,
    media_id UUID NOT NULL,
    document_kind VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_professional_verification_documents PRIMARY KEY (verification_id, media_id),
    CONSTRAINT fk_verification_documents_verification FOREIGN KEY (verification_id)
        REFERENCES professional_verifications(id) ON DELETE CASCADE,
    CONSTRAINT fk_verification_documents_media FOREIGN KEY (media_id)
        REFERENCES media(id) ON DELETE RESTRICT
);

ALTER TABLE messages
    ADD COLUMN client_message_id UUID NULL,
    ADD COLUMN edited_at TIMESTAMPTZ NULL,
    ADD COLUMN moderation_status VARCHAR(20) NOT NULL DEFAULT 'VISIBLE',
    ADD CONSTRAINT chk_messages_moderation_status CHECK (moderation_status IN ('VISIBLE', 'UNDER_REVIEW', 'HIDDEN'));

CREATE UNIQUE INDEX uq_messages_sender_client_id
    ON messages (sender_id, client_message_id)
    WHERE sender_id IS NOT NULL AND client_message_id IS NOT NULL;

ALTER TABLE reports
    ADD COLUMN message_id UUID NULL,
    DROP CONSTRAINT chk_reports_exactly_one_target,
    ADD CONSTRAINT chk_reports_exactly_one_target CHECK (
        num_nonnulls(target_user_id, post_id, comment_id, message_id) = 1
    ),
    ADD CONSTRAINT fk_reports_message FOREIGN KEY (message_id)
        REFERENCES messages(id) ON DELETE RESTRICT;

CREATE UNIQUE INDEX uq_open_report_by_reporter_target
    ON reports (reporter_id, COALESCE(target_user_id, post_id, comment_id, message_id))
    WHERE status IN ('PENDING', 'IN_REVIEW');

ALTER TABLE conversation_members
    ADD COLUMN archived_at TIMESTAMPTZ NULL;
