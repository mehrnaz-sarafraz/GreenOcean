package com.greenocean.backend.social.service;

import com.greenocean.backend.social.dto.SocialConnectionResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.time.OffsetDateTime;
import java.util.List;
import com.greenocean.backend.social.dto.FollowRequestResponse;

@Repository
public class SocialInteractionRepository {
    private final JdbcTemplate jdbcTemplate;

    public SocialInteractionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean likePost(UUID userId, UUID postId) {
        return jdbcTemplate.update("INSERT INTO post_likes (post_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING", postId, userId) == 1;
    }

    public void unlikePost(UUID userId, UUID postId) {
        jdbcTemplate.update("DELETE FROM post_likes WHERE post_id = ? AND user_id = ?", postId, userId);
    }

    public boolean likeComment(UUID userId, UUID commentId) {
        return jdbcTemplate.update("INSERT INTO comment_likes (comment_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING", commentId, userId) == 1;
    }

    public void unlikeComment(UUID userId, UUID commentId) {
        jdbcTemplate.update("DELETE FROM comment_likes WHERE comment_id = ? AND user_id = ?", commentId, userId);
    }

    public void bookmark(UUID userId, UUID postId) {
        jdbcTemplate.update("INSERT INTO bookmarks (user_id, post_id) VALUES (?, ?) ON CONFLICT DO NOTHING", userId, postId);
    }

    public void removeBookmark(UUID userId, UUID postId) {
        jdbcTemplate.update("DELETE FROM bookmarks WHERE user_id = ? AND post_id = ?", userId, postId);
    }

    public boolean follow(UUID followerId, UUID followingId) {
        return jdbcTemplate.update("INSERT INTO follows (follower_id, following_id) VALUES (?, ?) ON CONFLICT DO NOTHING", followerId, followingId) == 1;
    }

    public boolean isFollowing(UUID followerId, UUID followingId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM follows WHERE follower_id = ? AND following_id = ?)",
                Boolean.class, followerId, followingId);
        return Boolean.TRUE.equals(exists);
    }

    public boolean isProfilePrivate(UUID userId) {
        Boolean value = jdbcTemplate.queryForObject(
                "SELECT is_profile_private FROM profiles WHERE user_id = ?", Boolean.class, userId);
        return Boolean.TRUE.equals(value);
    }

    public boolean requestFollow(UUID requesterId, UUID targetId) {
        return jdbcTemplate.update("""
                INSERT INTO follow_requests (requester_id, target_id, status, created_at, resolved_at)
                VALUES (?, ?, 'PENDING', CURRENT_TIMESTAMP, NULL)
                ON CONFLICT (requester_id, target_id) DO UPDATE
                    SET status = 'PENDING', created_at = CURRENT_TIMESTAMP, resolved_at = NULL
                  WHERE follow_requests.status <> 'PENDING'
                """, requesterId, targetId) == 1;
    }

    public boolean hasPendingFollowRequest(UUID requesterId, UUID targetId) {
        Boolean exists = jdbcTemplate.queryForObject("""
                SELECT EXISTS(SELECT 1 FROM follow_requests
                               WHERE requester_id = ? AND target_id = ? AND status = 'PENDING')
                """, Boolean.class, requesterId, targetId);
        return Boolean.TRUE.equals(exists);
    }

    public boolean acceptFollowRequest(UUID requesterId, UUID targetId) {
        int updated = jdbcTemplate.update("""
                UPDATE follow_requests SET status = 'ACCEPTED', resolved_at = CURRENT_TIMESTAMP
                 WHERE requester_id = ? AND target_id = ? AND status = 'PENDING'
                """, requesterId, targetId);
        if (updated == 1) follow(requesterId, targetId);
        return updated == 1;
    }

    public boolean declineFollowRequest(UUID requesterId, UUID targetId) {
        return jdbcTemplate.update("""
                UPDATE follow_requests SET status = 'DECLINED', resolved_at = CURRENT_TIMESTAMP
                 WHERE requester_id = ? AND target_id = ? AND status = 'PENDING'
                """, requesterId, targetId) == 1;
    }

    public void cancelFollowRequest(UUID requesterId, UUID targetId) {
        jdbcTemplate.update("""
                UPDATE follow_requests SET status = 'CANCELLED', resolved_at = CURRENT_TIMESTAMP
                 WHERE requester_id = ? AND target_id = ? AND status = 'PENDING'
                """, requesterId, targetId);
    }

    public List<FollowRequestResponse> incomingFollowRequests(UUID targetId) {
        return jdbcTemplate.query("""
                SELECT p.user_id, p.display_name, p.username, p.avatar_url, fr.created_at
                  FROM follow_requests fr JOIN profiles p ON p.user_id = fr.requester_id
                 WHERE fr.target_id = ? AND fr.status = 'PENDING'
                 ORDER BY fr.created_at DESC
                """, this::followRequest, targetId);
    }

    public List<FollowRequestResponse> outgoingFollowRequests(UUID requesterId) {
        return jdbcTemplate.query("""
                SELECT p.user_id, p.display_name, p.username, p.avatar_url, fr.created_at
                  FROM follow_requests fr JOIN profiles p ON p.user_id = fr.target_id
                 WHERE fr.requester_id = ? AND fr.status = 'PENDING'
                 ORDER BY fr.created_at DESC
                """, this::followRequest, requesterId);
    }

    public void unfollow(UUID followerId, UUID followingId) {
        jdbcTemplate.update("DELETE FROM follows WHERE follower_id = ? AND following_id = ?", followerId, followingId);
        cancelFollowRequest(followerId, followingId);
    }

    public void block(UUID blockerId, UUID blockedId) {
        jdbcTemplate.update("INSERT INTO blocks (blocker_id, blocked_id) VALUES (?, ?) ON CONFLICT DO NOTHING", blockerId, blockedId);
        jdbcTemplate.update("DELETE FROM follows WHERE (follower_id = ? AND following_id = ?) OR (follower_id = ? AND following_id = ?)",
                blockerId, blockedId, blockedId, blockerId);
        jdbcTemplate.update("""
                DELETE FROM follow_requests
                 WHERE (requester_id = ? AND target_id = ?) OR (requester_id = ? AND target_id = ?)
                """, blockerId, blockedId, blockedId, blockerId);
    }

    public void unblock(UUID blockerId, UUID blockedId) {
        jdbcTemplate.update("DELETE FROM blocks WHERE blocker_id = ? AND blocked_id = ?", blockerId, blockedId);
    }

    public boolean isCommunityMember(UUID communityId, UUID userId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM community_members WHERE community_id = ? AND user_id = ?)",
                Boolean.class, communityId, userId);
        return Boolean.TRUE.equals(exists);
    }

    public boolean hasBlockBetween(UUID firstUserId, UUID secondUserId) {
        Boolean exists = jdbcTemplate.queryForObject("""
                SELECT EXISTS(
                    SELECT 1 FROM blocks
                     WHERE (blocker_id = ? AND blocked_id = ?)
                        OR (blocker_id = ? AND blocked_id = ?)
                )
                """, Boolean.class, firstUserId, secondUserId, secondUserId, firstUserId);
        return Boolean.TRUE.equals(exists);
    }

    public List<SocialConnectionResponse> following(UUID userId) {
        return jdbcTemplate.query("""
                SELECT p.user_id, p.display_name, p.username, p.avatar_url, f.created_at
                  FROM follows f JOIN profiles p ON p.user_id = f.following_id
                 WHERE f.follower_id = ? ORDER BY f.created_at DESC
                """, this::connection, userId);
    }

    public List<SocialConnectionResponse> followers(UUID userId) {
        return jdbcTemplate.query("""
                SELECT p.user_id, p.display_name, p.username, p.avatar_url, f.created_at
                  FROM follows f JOIN profiles p ON p.user_id = f.follower_id
                 WHERE f.following_id = ? ORDER BY f.created_at DESC
                """, this::connection, userId);
    }

    public List<SocialConnectionResponse> blocked(UUID userId) {
        return jdbcTemplate.query("""
                SELECT p.user_id, p.display_name, p.username, p.avatar_url, b.created_at
                  FROM blocks b JOIN profiles p ON p.user_id = b.blocked_id
                 WHERE b.blocker_id = ? ORDER BY b.created_at DESC
                """, this::connection, userId);
    }

    private SocialConnectionResponse connection(java.sql.ResultSet rs, int row) throws java.sql.SQLException {
        return new SocialConnectionResponse(rs.getObject("user_id", UUID.class), rs.getString("display_name"),
                rs.getString("username"), rs.getString("avatar_url"),
                rs.getObject("created_at", OffsetDateTime.class).toInstant());
    }

    public long followerCount(UUID userId) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM follows WHERE following_id = ?", Long.class, userId);
        return count == null ? 0 : count;
    }

    public long followingCount(UUID userId) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM follows WHERE follower_id = ?", Long.class, userId);
        return count == null ? 0 : count;
    }

    public long visiblePostCount(UUID authorId, UUID viewerId) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM posts p WHERE p.community_id IS NULL AND p.author_id = ? AND can_read_social_post(p.id, ?)
                AND (NOT p.is_anonymous OR p.author_id = ?)
                """, Long.class, authorId, viewerId, viewerId);
        return count == null ? 0 : count;
    }

    public long helpfulReactionCount(UUID authorId) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM post_likes likes
                  JOIN posts p ON p.id = likes.post_id
                 WHERE p.author_id = ? AND p.status = 'PUBLISHED'
                """, Long.class, authorId);
        return count == null ? 0 : count;
    }

    private FollowRequestResponse followRequest(java.sql.ResultSet rs, int row) throws java.sql.SQLException {
        return new FollowRequestResponse(rs.getObject("user_id", UUID.class), rs.getString("display_name"),
                rs.getString("username"), rs.getString("avatar_url"),
                rs.getObject("created_at", OffsetDateTime.class).toInstant());
    }
}
