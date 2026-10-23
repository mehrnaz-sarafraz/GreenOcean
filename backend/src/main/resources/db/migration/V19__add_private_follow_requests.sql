CREATE TABLE follow_requests (
    requester_id UUID NOT NULL,
    target_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ NULL,
    CONSTRAINT pk_follow_requests PRIMARY KEY (requester_id, target_id),
    CONSTRAINT chk_follow_requests_not_self CHECK (requester_id <> target_id),
    CONSTRAINT chk_follow_requests_status CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'CANCELLED')),
    CONSTRAINT fk_follow_requests_requester FOREIGN KEY (requester_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_follow_requests_target FOREIGN KEY (target_id)
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_follow_requests_target_pending
    ON follow_requests (target_id, created_at DESC)
    WHERE status = 'PENDING';

ALTER TABLE notifications
    DROP CONSTRAINT chk_notifications_type,
    ADD CONSTRAINT chk_notifications_type CHECK (
        type IN ('LIKE', 'COMMENT', 'REPLY', 'FOLLOW', 'FOLLOW_REQUEST', 'MENTION',
                 'PROFESSIONAL_REPLY', 'SYSTEM_NOTICE')
    );
