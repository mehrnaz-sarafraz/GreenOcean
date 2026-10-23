CREATE TABLE community_join_requests (
    community_id UUID NOT NULL,
    requester_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ NULL,
    resolved_by UUID NULL,
    CONSTRAINT pk_community_join_requests PRIMARY KEY (community_id, requester_id),
    CONSTRAINT chk_community_join_requests_status CHECK (status IN ('PENDING', 'APPROVED', 'DECLINED', 'CANCELLED')),
    CONSTRAINT fk_community_join_requests_community FOREIGN KEY (community_id)
        REFERENCES communities(id) ON DELETE CASCADE,
    CONSTRAINT fk_community_join_requests_requester FOREIGN KEY (requester_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_community_join_requests_resolver FOREIGN KEY (resolved_by)
        REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_community_join_requests_pending
    ON community_join_requests (community_id, requested_at)
    WHERE status = 'PENDING';
