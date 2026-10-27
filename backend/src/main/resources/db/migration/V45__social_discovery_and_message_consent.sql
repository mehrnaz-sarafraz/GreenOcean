CREATE INDEX social_named_following ON posts(author_id,created_at DESC,id DESC)
 WHERE status='PUBLISHED' AND NOT is_anonymous AND deleted_at IS NULL;
CREATE INDEX social_saved_owner ON bookmarks(user_id,created_at DESC,post_id);

-- Extend the existing conversation, never create a parallel messaging store.
-- Existing conversations preserve their established access.
ALTER TABLE conversations ADD COLUMN request_status VARCHAR(20) NOT NULL DEFAULT 'ACCEPTED'
 CHECK(request_status IN('PENDING','ACCEPTED','DECLINED','CANCELLED'));
ALTER TABLE conversations ADD COLUMN requester_id UUID REFERENCES users(id);
ALTER TABLE conversations ADD COLUMN request_target_id UUID REFERENCES users(id);
CREATE INDEX social_message_requests ON conversations(request_target_id,updated_at DESC) WHERE request_status='PENDING';
ALTER TABLE notifications DROP CONSTRAINT chk_notifications_type;
ALTER TABLE notifications ADD CONSTRAINT chk_notifications_type CHECK(type IN
 ('LIKE','COMMENT','REPLY','FOLLOW','FOLLOW_REQUEST','MENTION','PROFESSIONAL_REPLY','SYSTEM_NOTICE','MESSAGE_REQUEST','FOLLOW_ACCEPTED'));
