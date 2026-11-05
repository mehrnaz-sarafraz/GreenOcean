-- Existing reports retain their text, status and subjects; the context only narrows staff projection.
UPDATE reports r SET context_group_id=COALESCE(r.community_id,
 (SELECT community_id FROM posts WHERE id=r.post_id),
 (SELECT p.community_id FROM comments c JOIN posts p ON p.id=c.post_id WHERE c.id=r.comment_id),
 (SELECT community_id FROM community_events WHERE id=r.event_id))
WHERE context_group_id IS NULL;
CREATE INDEX group_open_reports ON reports(context_group_id,created_at,id) WHERE status IN('PENDING','IN_REVIEW');
