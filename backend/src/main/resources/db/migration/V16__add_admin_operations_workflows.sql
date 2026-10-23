CREATE TABLE broadcast_notices (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    created_by UUID NOT NULL,
    title VARCHAR(160) NOT NULL,
    body VARCHAR(2000) NOT NULL,
    audience VARCHAR(30) NOT NULL,
    category VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    recipient_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMPTZ NULL,
    CONSTRAINT chk_broadcast_audience CHECK (audience IN ('ALL','MEMBERS','PROFESSIONALS','LISTENERS')),
    CONSTRAINT chk_broadcast_category CHECK (category IN ('SAFETY','SERVICE','COMMUNITY','EDITORIAL')),
    CONSTRAINT chk_broadcast_status CHECK (status IN ('DRAFT','SENT','CANCELLED')),
    CONSTRAINT fk_broadcast_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
);

ALTER TABLE notifications
    DROP CONSTRAINT chk_notifications_type,
    ADD COLUMN title VARCHAR(160) NULL,
    ADD COLUMN body VARCHAR(2000) NULL,
    ADD COLUMN broadcast_id UUID NULL,
    ADD CONSTRAINT chk_notifications_type CHECK (
        type IN ('LIKE', 'COMMENT', 'REPLY', 'FOLLOW', 'MENTION', 'PROFESSIONAL_REPLY', 'SYSTEM_NOTICE')
    ),
    ADD CONSTRAINT fk_notifications_broadcast FOREIGN KEY (broadcast_id)
        REFERENCES broadcast_notices(id) ON DELETE SET NULL;

CREATE INDEX idx_broadcast_notices_created ON broadcast_notices (created_at DESC);
