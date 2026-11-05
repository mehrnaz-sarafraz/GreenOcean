ALTER TABLE community_events ADD COLUMN revision BIGINT NOT NULL DEFAULT 1;
ALTER TABLE community_events ADD COLUMN discussion_post_id UUID REFERENCES posts(id) ON DELETE SET NULL;
CREATE TABLE community_lifecycle_decisions(
 id UUID PRIMARY KEY DEFAULT uuidv7(),community_id UUID NOT NULL REFERENCES communities(id),
 actor_id UUID REFERENCES users(id),previous_status VARCHAR(20) NOT NULL,next_status VARCHAR(20) NOT NULL,
 reason VARCHAR(1000) NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE community_content_actions(
 id UUID PRIMARY KEY DEFAULT uuidv7(),community_id UUID NOT NULL REFERENCES communities(id),
 post_id UUID REFERENCES posts(id) ON DELETE CASCADE,comment_id UUID REFERENCES comments(id) ON DELETE CASCADE,
 revision BIGINT NOT NULL,actor_id UUID REFERENCES users(id),reason VARCHAR(1000) NOT NULL,
 status VARCHAR(16) NOT NULL DEFAULT 'HIDDEN' CHECK(status IN('HIDDEN','RESTORED')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK(num_nonnulls(post_id,comment_id)=1));
CREATE UNIQUE INDEX community_post_hidden ON community_content_actions(post_id,revision) WHERE status='HIDDEN';
CREATE UNIQUE INDEX community_comment_hidden ON community_content_actions(comment_id,revision) WHERE status='HIDDEN';
ALTER TABLE moderation_appeals ADD COLUMN group_action_id UUID UNIQUE REFERENCES community_content_actions(id) ON DELETE CASCADE;
ALTER TABLE reports ADD COLUMN event_id UUID REFERENCES community_events(id);
ALTER TABLE reports ADD COLUMN context_group_id UUID REFERENCES communities(id);
ALTER TABLE reports DROP CONSTRAINT chk_reports_exactly_one_target;
ALTER TABLE reports ADD CONSTRAINT chk_reports_exactly_one_target CHECK(num_nonnulls(target_user_id,post_id,comment_id,conversation_id,community_id,event_id)=1);
-- Preserve every historical audience. New group restrictions only narrow current reads.
CREATE OR REPLACE FUNCTION can_read_social_post(post_id UUID,viewer UUID) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT can_read_social_post_before_groups(post_id,viewer) AND EXISTS(SELECT 1 FROM posts p WHERE p.id=post_id
 AND (p.community_id IS NULL OR active_group_member(p.community_id,viewer))
 AND NOT EXISTS(SELECT 1 FROM community_content_actions a WHERE a.post_id=p.id AND a.revision=p.revision AND a.status='HIDDEN')) $$;
CREATE FUNCTION can_read_social_comment(comment_id UUID,viewer UUID) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT EXISTS(SELECT 1 FROM comments c JOIN users u ON u.id=c.author_id WHERE c.id=comment_id
 AND c.status='PUBLISHED' AND c.deleted_at IS NULL AND u.status='ACTIVE' AND u.deleted_at IS NULL
 AND can_read_social_post(c.post_id,viewer) AND NOT EXISTS(SELECT 1 FROM blocks b
 WHERE (b.blocker_id=viewer AND b.blocked_id=c.author_id) OR (b.blocker_id=c.author_id AND b.blocked_id=viewer)))
 AND NOT EXISTS(SELECT 1 FROM community_content_actions a JOIN comments c ON c.id=a.comment_id
 WHERE c.id=comment_id AND a.revision=c.revision AND a.status='HIDDEN') $$;
-- Closing/suspending through any authorized existing path revokes pending grants too.
CREATE FUNCTION stop_group_interactions() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN
 IF NEW.status IN('SUSPENDED','ARCHIVED') AND NEW.status IS DISTINCT FROM OLD.status THEN
  UPDATE community_invitations SET status='REVOKED' WHERE community_id=NEW.id AND status='PENDING';
  UPDATE community_ownership_transfers SET status='CANCELLED' WHERE community_id=NEW.id AND status='PENDING';
  UPDATE community_join_requests SET status='CANCELLED' WHERE community_id=NEW.id AND status='PENDING';
  UPDATE community_events SET status='CANCELLED',revision=revision+1 WHERE community_id=NEW.id AND status IN('SCHEDULED','LIVE');
 END IF;RETURN NEW;END $$;
CREATE TRIGGER group_stop_interactions AFTER UPDATE OF status ON communities FOR EACH ROW EXECUTE FUNCTION stop_group_interactions();
