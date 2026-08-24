CREATE TABLE communities (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    name VARCHAR(80) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    icon_url TEXT NULL,
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_communities_name UNIQUE (name),
    CONSTRAINT uq_communities_slug UNIQUE (slug),
    CONSTRAINT chk_communities_status CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    CONSTRAINT chk_communities_slug CHECK (
        slug = LOWER(slug)
        AND slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'
    ),
    CONSTRAINT fk_communities_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE RESTRICT
);

CREATE TABLE community_members (
    community_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_community_members PRIMARY KEY (community_id, user_id),
    CONSTRAINT chk_community_members_role
        CHECK (role IN ('MEMBER', 'MODERATOR', 'OWNER')),
    CONSTRAINT fk_community_members_community
        FOREIGN KEY (community_id)
        REFERENCES communities(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_community_members_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_community_members_user_id
    ON community_members (user_id);

CREATE TRIGGER trg_communities_set_updated_at
    BEFORE UPDATE ON communities
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
