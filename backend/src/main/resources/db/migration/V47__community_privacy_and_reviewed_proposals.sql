ALTER TABLE communities ADD COLUMN privacy_type VARCHAR(12);
UPDATE communities SET privacy_type=CASE WHEN is_private THEN 'PRIVATE' ELSE 'PUBLIC' END;
ALTER TABLE communities ALTER COLUMN privacy_type SET NOT NULL;
ALTER TABLE communities ADD CONSTRAINT community_privacy_type CHECK(privacy_type IN('PUBLIC','PRIVATE','HIDDEN'));
ALTER TABLE communities ADD COLUMN rules_version BIGINT NOT NULL DEFAULT 1;
ALTER TABLE communities DROP CONSTRAINT chk_communities_status;
ALTER TABLE communities ADD CONSTRAINT chk_communities_status CHECK(status IN('ACTIVE','RESTRICTED','SUSPENDED','ARCHIVED'));
CREATE FUNCTION fixed_group_privacy() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN
 IF NEW.privacy_type IS DISTINCT FROM OLD.privacy_type OR NEW.is_private IS DISTINCT FROM OLD.is_private THEN
  RAISE EXCEPTION 'Group privacy is fixed after activation'; END IF; RETURN NEW; END $$;
CREATE TRIGGER community_fixed_privacy BEFORE UPDATE ON communities FOR EACH ROW EXECUTE FUNCTION fixed_group_privacy();
CREATE FUNCTION initial_group_privacy() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN
 NEW.privacy_type=COALESCE(NEW.privacy_type,CASE WHEN NEW.is_private THEN 'PRIVATE' ELSE 'PUBLIC' END);
 NEW.is_private=NEW.privacy_type<>'PUBLIC';RETURN NEW;END $$;
CREATE TRIGGER community_initial_privacy BEFORE INSERT ON communities FOR EACH ROW EXECUTE FUNCTION initial_group_privacy();
CREATE TABLE community_proposals(
 id UUID PRIMARY KEY DEFAULT uuidv7(),owner_id UUID NOT NULL REFERENCES users(id),
 name VARCHAR(80) NOT NULL,description VARCHAR(500) NOT NULL,purpose VARCHAR(1000) NOT NULL,
 privacy_type VARCHAR(12) NOT NULL CHECK(privacy_type IN('PUBLIC','PRIVATE','HIDDEN')),
 rules JSONB NOT NULL,revision BIGINT NOT NULL DEFAULT 1,
 status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK(status IN('DRAFT','SUBMITTED','NEEDS_REVISION','APPROVED','REJECTED','CANCELLED')),
 reviewer_id UUID REFERENCES users(id),decision_note VARCHAR(1000),reviewed_at TIMESTAMPTZ,
 community_id UUID UNIQUE REFERENCES communities(id),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX community_proposal_owner ON community_proposals(owner_id,created_at,id);
CREATE TABLE community_proposal_reviews(
 id UUID PRIMARY KEY DEFAULT uuidv7(),proposal_id UUID NOT NULL REFERENCES community_proposals(id) ON DELETE CASCADE,
 revision BIGINT NOT NULL,outcome VARCHAR(20) NOT NULL,reviewer_id UUID REFERENCES users(id),
 note VARCHAR(1000) NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE FUNCTION active_group_member(group_id UUID,viewer UUID) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT EXISTS(SELECT 1 FROM communities g JOIN community_members m ON m.community_id=g.id
 JOIN users u ON u.id=m.user_id WHERE g.id=group_id AND m.user_id=viewer AND u.status='ACTIVE'
 AND u.deleted_at IS NULL AND g.status IN('ACTIVE','RESTRICTED')) $$;
CREATE FUNCTION visible_group(group_id UUID,viewer UUID) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT EXISTS(SELECT 1 FROM communities g WHERE g.id=group_id AND g.status IN('ACTIVE','RESTRICTED')
 AND (g.privacy_type<>'HIDDEN' OR active_group_member(g.id,viewer))) $$;
ALTER FUNCTION can_read_social_post(UUID,UUID) RENAME TO can_read_social_post_before_groups;
CREATE FUNCTION can_read_social_post(post_id UUID,viewer UUID) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT can_read_social_post_before_groups(post_id,viewer) AND EXISTS(SELECT 1 FROM posts p WHERE p.id=post_id
 AND (p.community_id IS NULL OR active_group_member(p.community_id,viewer))) $$;
