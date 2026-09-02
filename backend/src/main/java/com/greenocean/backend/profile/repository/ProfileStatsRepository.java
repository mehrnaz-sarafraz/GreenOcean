package com.greenocean.backend.profile.repository;

import com.greenocean.backend.profile.dto.ProfileStatsResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ProfileStatsRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProfileStatsRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public ProfileStatsResponse get(UUID userId) {
        return jdbcTemplate.queryForObject("""
                SELECT (SELECT COUNT(*) FROM follows WHERE following_id = ?) AS followers,
                       (SELECT COUNT(*) FROM follows WHERE follower_id = ?) AS following,
                       (SELECT COUNT(*) FROM posts WHERE author_id = ? AND status = 'PUBLISHED') AS stories,
                       (SELECT COUNT(*) FROM post_likes likes
                         JOIN posts p ON p.id = likes.post_id WHERE p.author_id = ?) +
                       (SELECT COUNT(*) FROM comment_likes likes
                         JOIN comments c ON c.id = likes.comment_id WHERE c.author_id = ?) AS helpful_reactions,
                       (SELECT COUNT(*) FROM bookmarks WHERE user_id = ?) AS saved_posts,
                       (SELECT COUNT(*) FROM blocks WHERE blocker_id = ?) AS blocked_accounts
                """, (rs, row) -> new ProfileStatsResponse(
                rs.getLong("followers"), rs.getLong("following"), rs.getLong("stories"),
                rs.getLong("helpful_reactions"), rs.getLong("saved_posts"), rs.getLong("blocked_accounts")),
                userId, userId, userId, userId, userId, userId, userId);
    }
}
