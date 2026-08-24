CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    reporter_id UUID NOT NULL,
    target_user_id UUID NULL,
    post_id UUID NULL,
    comment_id UUID NULL,
    reason VARCHAR(40) NOT NULL,
    description VARCHAR(1000) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    assigned_to UUID NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ NULL,
    resolution_note VARCHAR(1000) NULL,

    CONSTRAINT chk_reports_exactly_one_target CHECK (
        num_nonnulls(target_user_id, post_id, comment_id) = 1
    ),
    CONSTRAINT chk_reports_reason CHECK (
        reason IN (
            'HARASSMENT', 'SPAM', 'HATE_SPEECH', 'SEXUAL_CONTENT',
            'SELF_HARM_CONTENT', 'MISINFORMATION', 'OTHER'
        )
    ),
    CONSTRAINT chk_reports_status CHECK (
        status IN ('PENDING', 'IN_REVIEW', 'RESOLVED', 'DISMISSED')
    ),
    CONSTRAINT chk_reports_resolution CHECK (
        (status IN ('PENDING', 'IN_REVIEW') AND resolved_at IS NULL)
        OR (status IN ('RESOLVED', 'DISMISSED') AND resolved_at IS NOT NULL)
    ),
    CONSTRAINT fk_reports_reporter
        FOREIGN KEY (reporter_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_reports_target_user
        FOREIGN KEY (target_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_reports_post
        FOREIGN KEY (post_id)
        REFERENCES posts(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_reports_comment
        FOREIGN KEY (comment_id)
        REFERENCES comments(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_reports_assigned_to
        FOREIGN KEY (assigned_to)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_reports_status_created_at
    ON reports (status, created_at ASC);

CREATE INDEX idx_reports_assigned_to_status
    ON reports (assigned_to, status);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    actor_user_id UUID NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NULL,
    metadata JSONB NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_logs_actor
        FOREIGN KEY (actor_user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_audit_logs_entity
    ON audit_logs (entity_type, entity_id, created_at DESC);
