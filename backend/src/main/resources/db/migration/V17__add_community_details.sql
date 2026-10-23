CREATE TABLE community_rules (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    community_id UUID NOT NULL,
    position SMALLINT NOT NULL,
    rule_text VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_community_rules_position UNIQUE (community_id, position),
    CONSTRAINT chk_community_rules_position CHECK (position BETWEEN 1 AND 50),
    CONSTRAINT fk_community_rules_community
        FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE
);

CREATE TABLE community_events (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    community_id UUID NOT NULL,
    host_user_id UUID NULL,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(1000) NULL,
    format VARCHAR(30) NOT NULL DEFAULT 'TEXT_CIRCLE',
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    capacity INTEGER NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_community_events_format
        CHECK (format IN ('TEXT_CIRCLE', 'VIDEO', 'AUDIO', 'WORKSHOP', 'Q_AND_A')),
    CONSTRAINT chk_community_events_status
        CHECK (status IN ('SCHEDULED', 'LIVE', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_community_events_dates CHECK (ends_at > starts_at),
    CONSTRAINT chk_community_events_capacity CHECK (capacity IS NULL OR capacity > 0),
    CONSTRAINT fk_community_events_community
        FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE,
    CONSTRAINT fk_community_events_host
        FOREIGN KEY (host_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_community_events_community_starts
    ON community_events (community_id, starts_at);

CREATE TABLE community_event_attendees (
    event_id UUID NOT NULL,
    user_id UUID NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_community_event_attendees PRIMARY KEY (event_id, user_id),
    CONSTRAINT fk_community_event_attendees_event
        FOREIGN KEY (event_id) REFERENCES community_events(id) ON DELETE CASCADE,
    CONSTRAINT fk_community_event_attendees_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_community_event_attendees_user
    ON community_event_attendees (user_id);

CREATE TRIGGER trg_community_events_set_updated_at
    BEFORE UPDATE ON community_events
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

INSERT INTO community_rules (id, community_id, position, rule_text) VALUES
('0191e000-0000-7000-8000-000000000001', '01912000-0000-7000-8000-000000000001', 1, 'Share lived experience; do not diagnose other members.'),
('0191e000-0000-7000-8000-000000000002', '01912000-0000-7000-8000-000000000001', 2, 'Use content notes for potentially distressing experiences.'),
('0191e000-0000-7000-8000-000000000003', '01912000-0000-7000-8000-000000000001', 3, 'Protect privacy and do not repost another member''s story.'),
('0191e000-0000-7000-8000-000000000004', '01912000-0000-7000-8000-000000000002', 1, 'Offer support without diagnosing, prescribing, or promising a cure.'),
('0191e000-0000-7000-8000-000000000005', '01912000-0000-7000-8000-000000000002', 2, 'No graphic instructions, threats, harassment, or hate.'),
('0191e000-0000-7000-8000-000000000006', '01912000-0000-7000-8000-000000000002', 3, 'Encourage urgent local help when someone may be in immediate danger.'),
('0191e000-0000-7000-8000-000000000007', '01912000-0000-7000-8000-000000000003', 1, 'Speak from your own experience and respect different boundaries.'),
('0191e000-0000-7000-8000-000000000008', '01912000-0000-7000-8000-000000000003', 2, 'Do not identify another person or publish private conversations.'),
('0191e000-0000-7000-8000-000000000009', '01912000-0000-7000-8000-000000000003', 3, 'Disagreement is allowed; humiliation and coercion are not.'),
('0191e000-0000-7000-8000-000000000010', '01912000-0000-7000-8000-000000000004', 1, 'Protect every member''s privacy inside and outside this circle.'),
('0191e000-0000-7000-8000-000000000011', '01912000-0000-7000-8000-000000000004', 2, 'Recovery experiences are personal; avoid universal treatment claims.'),
('0191e000-0000-7000-8000-000000000012', '01912000-0000-7000-8000-000000000004', 3, 'Contact a moderator when content feels unsafe or pressuring.')
ON CONFLICT (community_id, position) DO NOTHING;

INSERT INTO community_events
    (id, community_id, host_user_id, title, description, format, starts_at, ends_at, capacity, status)
VALUES
('0191e100-0000-7000-8000-000000000001', '01912000-0000-7000-8000-000000000001', '01910000-0000-7000-8000-000000000005',
 'Gentle sleep reset', 'A moderated text circle for choosing one sustainable sleep-rhythm experiment.', 'TEXT_CIRCLE',
 CURRENT_TIMESTAMP + INTERVAL '2 days', CURRENT_TIMESTAMP + INTERVAL '2 days 1 hour', 60, 'SCHEDULED'),
('0191e100-0000-7000-8000-000000000002', '01912000-0000-7000-8000-000000000002', '01910000-0000-7000-8000-000000000004',
 'Ask a psychologist: worry loops', 'A general educational Q&A about worry loops, avoidance, and when to seek individual care.', 'Q_AND_A',
 CURRENT_TIMESTAMP + INTERVAL '4 days', CURRENT_TIMESTAMP + INTERVAL '4 days 1 hour', 100, 'SCHEDULED'),
('0191e100-0000-7000-8000-000000000003', '01912000-0000-7000-8000-000000000003', '01910000-0000-7000-8000-000000000006',
 'Boundaries practice workshop', 'Practice short, respectful boundary statements in a facilitated workshop.', 'WORKSHOP',
 CURRENT_TIMESTAMP + INTERVAL '6 days', CURRENT_TIMESTAMP + INTERVAL '6 days 1 hour 30 minutes', 40, 'SCHEDULED')
ON CONFLICT (id) DO NOTHING;
