ALTER TABLE community_content_actions DROP CONSTRAINT community_content_actions_status_check;
ALTER TABLE community_content_actions ADD CONSTRAINT community_content_actions_status_check
 CHECK(status IN('HIDDEN','RESTORED','SUPERSEDED'));
ALTER TABLE moderation_appeals DROP CONSTRAINT chk_moderation_appeals_status;
ALTER TABLE moderation_appeals ADD CONSTRAINT chk_moderation_appeals_status
 CHECK(status IN('OPEN','REVIEWING','UPHELD','OVERTURNED','SUPERSEDED'));
CREATE INDEX group_evidence_post ON community_content_actions(post_id);
CREATE INDEX group_evidence_comment ON community_content_actions(comment_id);
-- No original narrative is copied into another retention store. An edited/deleted
-- revision is obsolete evidence, never a fictitious completed human review.
CREATE FUNCTION supersede_group_evidence() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN
 UPDATE community_content_actions a SET status='SUPERSEDED'
 WHERE a.status='HIDDEN' AND ((TG_TABLE_NAME='posts' AND a.post_id=NEW.id)
 OR (TG_TABLE_NAME='comments' AND a.comment_id=NEW.id))
 AND (a.revision<>NEW.revision OR NEW.status='DELETED');
 UPDATE moderation_appeals ap SET status='SUPERSEDED'
 WHERE ap.status IN('OPEN','REVIEWING') AND EXISTS(SELECT 1 FROM community_content_actions a
 WHERE a.id=ap.group_action_id AND a.status='SUPERSEDED'
 AND ((TG_TABLE_NAME='posts' AND a.post_id=NEW.id) OR(TG_TABLE_NAME='comments' AND a.comment_id=NEW.id)));
 RETURN NEW;END $$;
CREATE TRIGGER post_supersede_group_evidence AFTER UPDATE OF revision,status ON posts
 FOR EACH ROW WHEN(OLD.revision IS DISTINCT FROM NEW.revision OR NEW.status='DELETED') EXECUTE FUNCTION supersede_group_evidence();
CREATE TRIGGER comment_supersede_group_evidence AFTER UPDATE OF revision,status ON comments
 FOR EACH ROW WHEN(OLD.revision IS DISTINCT FROM NEW.revision OR NEW.status='DELETED') EXECUTE FUNCTION supersede_group_evidence();
UPDATE community_content_actions a SET status='SUPERSEDED' WHERE a.status='HIDDEN'
 AND (EXISTS(SELECT 1 FROM posts p WHERE p.id=a.post_id AND (p.revision<>a.revision OR p.status='DELETED'))
 OR EXISTS(SELECT 1 FROM comments c WHERE c.id=a.comment_id AND (c.revision<>a.revision OR c.status='DELETED')));
UPDATE moderation_appeals ap SET status='SUPERSEDED' WHERE ap.status IN('OPEN','REVIEWING')
 AND EXISTS(SELECT 1 FROM community_content_actions a WHERE a.id=ap.group_action_id AND a.status='SUPERSEDED');
