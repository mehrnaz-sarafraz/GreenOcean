package com.greenocean.backend.social.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

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

    public void unfollow(UUID followerId, UUID followingId) {
        jdbcTemplate.update("DELETE FROM follows WHERE follower_id = ? AND following_id = ?", followerId, followingId);
    }

    public void block(UUID blockerId, UUID blockedId) {
        jdbcTemplate.update("INSERT INTO blocks (blocker_id, blocked_id) VALUES (?, ?) ON CONFLICT DO NOTHING", blockerId, blockedId);
        jdbcTemplate.update("DELETE FROM follows WHERE (follower_id = ? AND following_id = ?) OR (follower_id = ? AND following_id = ?)",
                blockerId, blockedId, blockedId, blockerId);
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
}
