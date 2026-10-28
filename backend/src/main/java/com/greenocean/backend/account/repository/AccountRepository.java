package com.greenocean.backend.account.repository;

import com.greenocean.backend.account.dto.AccountActivityResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class AccountRepository {
    private final JdbcTemplate jdbcTemplate;

    public AccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AccountActivityResponse activity(UUID userId) {
        List<AccountActivityResponse.PostItem> posts = jdbcTemplate.query("""
                SELECT p.id, p.body, c.name AS category, p.post_type, p.visibility, p.created_at
                  FROM posts p LEFT JOIN support_categories c ON c.id = p.category_id
                 WHERE p.author_id = ? AND p.deleted_at IS NULL ORDER BY p.created_at DESC
                """, (rs, row) -> new AccountActivityResponse.PostItem(
                rs.getObject("id", UUID.class), rs.getString("body"), rs.getString("category"),
                rs.getString("post_type"), rs.getString("visibility"),
                rs.getObject("created_at", OffsetDateTime.class).toInstant()), userId);

        List<AccountActivityResponse.CommentItem> comments = jdbcTemplate.query("""
                SELECT id, post_id, body, created_at FROM comments
                 WHERE author_id = ? AND deleted_at IS NULL ORDER BY created_at DESC
                """, (rs, row) -> new AccountActivityResponse.CommentItem(
                rs.getObject("id", UUID.class), rs.getObject("post_id", UUID.class), rs.getString("body"),
                rs.getObject("created_at", OffsetDateTime.class).toInstant()), userId);

        List<AccountActivityResponse.CommunityMembership> communities = jdbcTemplate.query("""
                SELECT c.id, c.name, c.slug, m.role, m.joined_at
                  FROM community_members m JOIN communities c ON c.id = m.community_id
                 WHERE m.user_id = ? ORDER BY m.joined_at DESC
                """, (rs, row) -> new AccountActivityResponse.CommunityMembership(
                rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("slug"), rs.getString("role"),
                rs.getObject("joined_at", OffsetDateTime.class).toInstant()), userId);

        List<AccountActivityResponse.SavedItem> saved = new ArrayList<>();
        saved.addAll(jdbcTemplate.query("""
                SELECT p.id, LEFT(p.body, 160) AS title, b.created_at FROM bookmarks b
                  JOIN posts p ON p.id = b.post_id WHERE b.user_id = ? AND p.deleted_at IS NULL
                   AND can_read_social_post(p.id, b.user_id)
                 ORDER BY b.created_at DESC
                """, (rs, row) -> new AccountActivityResponse.SavedItem("POST", rs.getObject("id", UUID.class),
                rs.getString("title"), rs.getObject("created_at", OffsetDateTime.class).toInstant()), userId));
        saved.addAll(jdbcTemplate.query("""
                SELECT m.id, m.title, s.created_at FROM media_saves s
                  JOIN media_recommendations m ON m.id = s.media_id WHERE s.user_id = ?
                 ORDER BY s.created_at DESC
                """, (rs, row) -> new AccountActivityResponse.SavedItem("MEDIA", rs.getObject("id", UUID.class),
                rs.getString("title"), rs.getObject("created_at", OffsetDateTime.class).toInstant()), userId));
        saved.addAll(jdbcTemplate.query("""
                SELECT a.id, a.title, s.created_at FROM article_saves s
                  JOIN professional_articles a ON a.id = s.article_id WHERE s.user_id = ?
                 ORDER BY s.created_at DESC
                """, (rs, row) -> new AccountActivityResponse.SavedItem("ARTICLE", rs.getObject("id", UUID.class),
                rs.getString("title"), rs.getObject("created_at", OffsetDateTime.class).toInstant()), userId));

        var sessions=jdbcTemplate.query("SELECT id,status,reason_code,CASE WHEN member_id=? THEN 'MEMBER' ELSE 'LISTENER' END AS participant_role,created_at,ended_at FROM live_support_requests WHERE member_id=? OR listener_id=? ORDER BY created_at DESC",(rs,n)->new AccountActivityResponse.ListenerSession(rs.getObject("id",UUID.class),rs.getString("status"),rs.getString("reason_code"),rs.getString("participant_role"),rs.getObject("created_at",OffsetDateTime.class).toInstant(),rs.getObject("ended_at",OffsetDateTime.class)==null?null:rs.getObject("ended_at",OffsetDateTime.class).toInstant()),userId,userId,userId);
        var ownMessages=jdbcTemplate.query("SELECT m.id,m.conversation_id,m.body,m.created_at FROM messages m JOIN conversations c ON c.id=m.conversation_id WHERE m.sender_id=? AND c.service_kind='LISTENER' AND m.deleted_at IS NULL ORDER BY m.created_at",(rs,n)->new AccountActivityResponse.ListenerMessage(rs.getObject("id",UUID.class),rs.getObject("conversation_id",UUID.class),rs.getString("body"),rs.getObject("created_at",OffsetDateTime.class).toInstant()),userId);
        return new AccountActivityResponse(posts, comments, communities, List.copyOf(saved),sessions,ownMessages);
    }

    public java.util.List<String> professionalDocumentKeys(UUID userId) {
        return jdbcTemplate.query("""
                SELECT d.storage_key FROM professional_application_documents d
                  JOIN professional_verifications v ON v.id = d.application_id
                 WHERE v.professional_user_id = ?
                """, (rs, row) -> rs.getString("storage_key"), userId);
    }

    public void erase(UUID userId) {
        jdbcTemplate.queryForList("SELECT id FROM users WHERE id=? FOR NO KEY UPDATE",userId);
        jdbcTemplate.queryForList("SELECT g.id FROM communities g WHERE g.id IN(SELECT community_id FROM community_members WHERE user_id=? UNION SELECT community_id FROM posts WHERE author_id=? UNION SELECT p.community_id FROM comments c JOIN posts p ON p.id=c.post_id WHERE c.author_id=?) ORDER BY g.id FOR UPDATE",userId,userId,userId);
        jdbcTemplate.update("DELETE FROM community_content_actions WHERE post_id IN(SELECT id FROM posts WHERE author_id=?) OR comment_id IN(SELECT id FROM comments WHERE author_id=?)",userId,userId);
        jdbcTemplate.update("UPDATE community_content_actions SET actor_id=NULL WHERE actor_id=?",userId);
        jdbcTemplate.update("UPDATE community_lifecycle_decisions SET actor_id=NULL WHERE actor_id=?",userId);
        jdbcTemplate.update("DELETE FROM community_member_actions WHERE user_id=?",userId);
        jdbcTemplate.update("UPDATE community_member_actions SET actor_id=NULL WHERE actor_id=?",userId);
        jdbcTemplate.update("DELETE FROM community_invitations WHERE target_id=? OR inviter_id=?",userId,userId);
        jdbcTemplate.update("DELETE FROM community_ownership_transfers WHERE target_id=? OR from_owner=?",userId,userId);
        jdbcTemplate.update("DELETE FROM community_bans WHERE user_id=?",userId);
        jdbcTemplate.update("UPDATE community_bans SET actor_id=NULL WHERE actor_id=?",userId);
        jdbcTemplate.update("UPDATE community_rule_revisions SET actor_id=NULL WHERE actor_id=?",userId);
        jdbcTemplate.update("DELETE FROM community_proposals WHERE owner_id=?",userId);
        jdbcTemplate.update("UPDATE conversations SET request_status='CANCELLED' WHERE request_status='PENDING' AND (requester_id=? OR request_target_id=?)",userId,userId);
        jdbcTemplate.update("UPDATE conversations SET requester_id=NULL,request_target_id=NULL WHERE requester_id=? OR request_target_id=?",userId,userId);
        jdbcTemplate.update("DELETE FROM moderation_appeals WHERE appellant_user_id=?",userId);
        jdbcTemplate.update("DELETE FROM social_drafts WHERE owner_id=?",userId);
        jdbcTemplate.update("DELETE FROM social_assessments WHERE post_id IN(SELECT id FROM posts WHERE author_id=?) OR comment_id IN(SELECT id FROM comments WHERE author_id=?)",userId,userId);
        jdbcTemplate.update("DELETE FROM processing_authorizations WHERE user_id=?",userId);
        jdbcTemplate.update("DELETE FROM privacy_notice_receipts WHERE user_id=?",userId);
        jdbcTemplate.update("DELETE FROM data_rights_requests WHERE user_id=?",userId);
        jdbcTemplate.queryForList("SELECT pg_advisory_xact_lock(72491001)");
        var planningEntities=jdbcTemplate.queryForList("SELECT id FROM listener_bookings WHERE member_id=? OR listener_id=? UNION SELECT id FROM listener_wait_queue WHERE member_id=? OR listener_id=? UNION SELECT e.id FROM listener_session_extensions e JOIN live_support_requests r ON r.id=e.session_id WHERE r.member_id=? OR r.listener_id=?",UUID.class,userId,userId,userId,userId,userId,userId);
        for(UUID entity:planningEntities){jdbcTemplate.update("DELETE FROM notifications WHERE id IN(SELECT notification_id FROM listener_support_notices WHERE entity_id=? AND notification_id IS NOT NULL)",entity);jdbcTemplate.update("DELETE FROM listener_support_notices WHERE entity_id=?",entity);}
        jdbcTemplate.update("DELETE FROM listener_wait_queue WHERE member_id=? OR listener_id=?",userId,userId);
        jdbcTemplate.update("DELETE FROM listener_queue_coverage_windows WHERE listener_id=?",userId);
        jdbcTemplate.update("UPDATE listener_queue_coverage_windows SET approved_by=NULL,revoked_at=COALESCE(revoked_at,listener_support_now()) WHERE approved_by=?",userId);
        jdbcTemplate.update("DELETE FROM listener_support_notices WHERE user_id=?",userId);
        jdbcTemplate.update("DELETE FROM listener_bookings WHERE member_id=? OR listener_id=?",userId,userId);
        jdbcTemplate.update("DELETE FROM listener_calendar_rules WHERE listener_id=?",userId);
        jdbcTemplate.update("DELETE FROM listener_calendar_exceptions WHERE listener_id=?",userId);
        jdbcTemplate.update("DELETE FROM listener_calendar_settings WHERE listener_id=?",userId);
        jdbcTemplate.update("DELETE FROM listener_presence WHERE listener_id=?",userId);
        jdbcTemplate.update("DELETE FROM listener_live_authorizations WHERE listener_id=?",userId);
        jdbcTemplate.update("DELETE FROM live_support_requests WHERE member_id=? OR listener_id=?",userId,userId);
        jdbcTemplate.update("DELETE FROM webauthn_ceremonies WHERE user_id=?",userId);
        jdbcTemplate.update("DELETE FROM webauthn_credentials WHERE user_id=?",userId);
        jdbcTemplate.update("DELETE FROM webauthn_enrollment_permits WHERE user_id=?",userId);
        jdbcTemplate.update("DELETE FROM audit_logs WHERE actor_user_id=? AND entity_type='SECURITY'",userId);
        jdbcTemplate.update("INSERT INTO account_deletions(user_id,status) VALUES (?,'PROCESSING') ON CONFLICT(user_id) DO UPDATE SET status='PROCESSING'",userId);
        jdbcTemplate.update("DELETE FROM email_outbox WHERE owner_user_id=? OR recipient=(SELECT email FROM users WHERE id=?)",userId,userId);
        jdbcTemplate.update("DELETE FROM email_verification_codes WHERE user_id=?",userId);
        jdbcTemplate.update("DELETE FROM password_reset_tokens WHERE user_id=?",userId);
        jdbcTemplate.update("DELETE FROM account_eligibility WHERE user_id=?",userId);
        jdbcTemplate.update("DELETE FROM policy_acceptances WHERE user_id=?",userId);
        jdbcTemplate.update("UPDATE support_requests SET email='deleted@greenocean.invalid',message='[Deleted]',user_id=NULL WHERE user_id=? OR email=(SELECT email FROM users WHERE id=?)",userId,userId);
        jdbcTemplate.update("DELETE FROM user_search_history WHERE user_id=?",userId);
        jdbcTemplate.update("UPDATE conversations SET title='Deleted member',subtitle=NULL,is_active=FALSE WHERE id IN (SELECT conversation_id FROM conversation_members WHERE user_id=?) AND conversation_kind <> 'GROUP'",userId);
        jdbcTemplate.update("UPDATE account_deletions SET status='COMPLETED',completed_at=CURRENT_TIMESTAMP WHERE user_id=?",userId);
        jdbcTemplate.update("UPDATE communities SET status = 'ARCHIVED' WHERE id IN(SELECT community_id FROM community_members WHERE user_id=? AND role='OWNER')", userId);
        jdbcTemplate.update("DELETE FROM promotion_campaigns WHERE professional_user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM consultation_requests WHERE requester_user_id = ? OR professional_user_id = ?", userId, userId);
        jdbcTemplate.update("DELETE FROM professional_reviews WHERE reviewer_user_id = ? OR professional_user_id = ?", userId, userId);
        jdbcTemplate.update("DELETE FROM professional_answers WHERE professional_user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM post_resource_attachments WHERE proposed_by=? OR reviewed_by=? OR post_id IN(SELECT id FROM posts WHERE author_id=?)",userId,userId,userId);
        jdbcTemplate.update("DELETE FROM post_resource_preferences WHERE post_id IN(SELECT id FROM posts WHERE author_id=?)",userId);
        jdbcTemplate.update("DELETE FROM growth_resource_saves WHERE user_id=?",userId);
        jdbcTemplate.update("DELETE FROM learning_lesson_views WHERE owner_id=?",userId);
        jdbcTemplate.update("DELETE FROM learning_lesson_completions WHERE owner_id=?",userId);
        jdbcTemplate.update("DELETE FROM learning_exercise_completions WHERE owner_id=?",userId);
        jdbcTemplate.update("DELETE FROM learning_course_completions WHERE owner_id=?",userId);
        jdbcTemplate.update("DELETE FROM wellbeing_prompt_versions p WHERE author_id=? AND NOT EXISTS(SELECT 1 FROM wellbeing_daily_questions q WHERE q.prompt_id=p.id)",userId);
        jdbcTemplate.update("UPDATE wellbeing_prompt_versions SET status='RETIRED',review_state='RETIRED',author_id=CASE WHEN author_id=? THEN NULL ELSE author_id END,reviewer_id=CASE WHEN reviewer_id=? THEN NULL ELSE reviewer_id END,review_note='Editorial account erased' WHERE author_id=? OR reviewer_id=?",userId,userId,userId,userId);
        jdbcTemplate.update("UPDATE guided_growth_paths SET author_name='Removed author',is_published=FALSE WHERE resource_id IN(SELECT id FROM growth_resources WHERE author_id=?)",userId);
        jdbcTemplate.update("DELETE FROM growth_resources WHERE author_id=?",userId);
        jdbcTemplate.update("UPDATE growth_resources SET retired=TRUE,featured=FALSE WHERE id IN(SELECT resource_id FROM growth_resource_versions WHERE reviewer_id=?)",userId);
        jdbcTemplate.update("UPDATE growth_resource_versions SET state='RETIRED',reviewer_id=NULL WHERE reviewer_id=?",userId);
        jdbcTemplate.update("UPDATE growth_resource_versions SET published_by=NULL WHERE published_by=?",userId);
        jdbcTemplate.update("UPDATE growth_editorial_events SET actor_id=NULL WHERE actor_id=?",userId);
        jdbcTemplate.update("DELETE FROM educational_permissions WHERE user_id=?",userId);
        jdbcTemplate.update("UPDATE educational_permissions SET issued_by=NULL,revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP) WHERE issued_by=?",userId);
        jdbcTemplate.update("DELETE FROM educational_credentials WHERE user_id=?",userId);
        jdbcTemplate.update("UPDATE educational_credentials SET verified_by=NULL,revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP) WHERE verified_by=?",userId);
        jdbcTemplate.update("DELETE FROM professional_articles WHERE author_id = ?", userId);
        jdbcTemplate.update("DELETE FROM professional_verifications WHERE professional_user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM professional_profiles WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM listener_applications WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM listener_profiles WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM wellbeing_daily_sets WHERE user_id=?",userId);
        jdbcTemplate.update("DELETE FROM wellbeing_journal_drafts WHERE user_id=?",userId);
        jdbcTemplate.update("DELETE FROM wellbeing_check_ins WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM wellbeing_journal_entries WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM wellbeing_goals WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM wellbeing_reflections WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM user_growth_path_progress WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM bookmarks WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM article_saves WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM media_saves WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM post_likes WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM comment_likes WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM notifications WHERE user_id = ? OR actor_user_id = ?", userId, userId);
        jdbcTemplate.update("DELETE FROM follows WHERE follower_id = ? OR following_id = ?", userId, userId);
        jdbcTemplate.update("DELETE FROM follow_requests WHERE requester_id = ? OR target_id = ?", userId, userId);
        jdbcTemplate.update("DELETE FROM blocks WHERE blocker_id = ? OR blocked_id = ?", userId, userId);
        jdbcTemplate.update("DELETE FROM community_event_attendees WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM community_join_requests WHERE requester_id = ?", userId);
        jdbcTemplate.update("DELETE FROM community_members WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM conversation_members WHERE user_id = ?", userId);
        jdbcTemplate.update("UPDATE messages SET body = '[Message removed]', deleted_at = CURRENT_TIMESTAMP WHERE sender_id = ?", userId);
        jdbcTemplate.update("UPDATE comments SET body = '[Deleted]', status = 'DELETED', deleted_at = CURRENT_TIMESTAMP WHERE author_id = ?", userId);
        jdbcTemplate.update("UPDATE posts SET body = '[Deleted]', status = 'DELETED', deleted_at = CURRENT_TIMESTAMP WHERE author_id = ?", userId);
        jdbcTemplate.update("DELETE FROM user_sessions WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM user_preferences WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", userId);
        String suffix = userId.toString().replace("-", "").substring(0, 16);
        jdbcTemplate.update("""
                UPDATE profiles SET username = ?, display_name = 'Deleted member', bio = NULL, avatar_url = NULL,
                       country_code = NULL, city = NULL, birth_year = NULL, is_profile_private = TRUE,
                       show_location = FALSE, show_birth_year = FALSE
                 WHERE user_id = ?
                """, "deleted_" + suffix, userId);
        jdbcTemplate.update("""
                UPDATE users SET email = ?, password_hash = 'DELETED', status = 'DELETED',
                       email_verified = FALSE, deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                 WHERE id = ?
                """, "deleted+" + suffix + "@greenocean.invalid", userId);
    }
}
