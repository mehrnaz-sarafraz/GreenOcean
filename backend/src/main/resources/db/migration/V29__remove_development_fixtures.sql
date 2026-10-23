-- Remove old demonstration identities and their generated activity without touching real accounts.

DELETE FROM reports
 WHERE reporter_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid')
    OR target_user_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid')
    OR post_id IN (SELECT id FROM posts WHERE author_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid'))
    OR comment_id IN (SELECT id FROM comments WHERE author_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid'));

DELETE FROM audit_logs
 WHERE actor_user_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid')
    OR metadata ->> 'source' = 'seed';

DELETE FROM broadcast_notices
 WHERE created_by IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid');

DELETE FROM export_jobs
 WHERE requested_by IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid');

DELETE FROM consultation_requests
 WHERE requester_user_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid')
    OR professional_user_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid');

DELETE FROM professional_answers
 WHERE professional_user_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid')
    OR post_id IN (SELECT id FROM posts WHERE author_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid'));

DELETE FROM professional_articles
 WHERE author_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid');

DELETE FROM notifications
 WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid')
    OR actor_user_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid')
    OR post_id IN (SELECT id FROM posts WHERE author_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid'))
    OR comment_id IN (SELECT id FROM comments WHERE author_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid'));

DELETE FROM messages
 WHERE sender_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid')
    OR id IN (
        '01919000-0000-7000-8000-000000000001',
        '01919000-0000-7000-8000-000000000004'
    );

DELETE FROM conversations c
 WHERE c.conversation_kind <> 'GROUP'
   AND EXISTS (
       SELECT 1 FROM conversation_members cm
        WHERE cm.conversation_id = c.id
          AND cm.user_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid')
   );

DELETE FROM comments
 WHERE author_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid');

DELETE FROM media
 WHERE uploader_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid');

DELETE FROM posts
 WHERE author_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid');

DELETE FROM communities
 WHERE created_by IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid');

DELETE FROM professional_profiles
 WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%@greenocean.invalid');

DELETE FROM users
 WHERE email LIKE '%@greenocean.invalid';

UPDATE media_recommendations
   SET recommended_by = 'GreenOcean editorial team'
 WHERE recommended_by IN ('Dr. Maya Bennett', 'Dr. Daniel Green');

UPDATE support_channels
   SET next_event = REPLACE(next_event, 'Â·', '-')
 WHERE next_event LIKE '%Â·%';
