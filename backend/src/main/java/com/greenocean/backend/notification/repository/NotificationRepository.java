package com.greenocean.backend.notification.repository;

import com.greenocean.backend.notification.dto.NotificationResponse;
import com.greenocean.backend.post.dto.AuthorSummary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NotificationRepository {
    private final JdbcTemplate jdbcTemplate;

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(UUID userId, UUID visibleActorId, String type, UUID postId, UUID commentId) {
        jdbcTemplate.update("""
                INSERT INTO notifications (user_id, actor_user_id, type, post_id, comment_id)
                VALUES (?, ?, ?, ?, ?)
                """, userId, visibleActorId, type, postId, commentId);
    }

    public Optional<UUID> postAuthor(UUID postId) {
        return jdbcTemplate.query("SELECT author_id FROM posts WHERE id = ?", (rs, row) ->
                rs.getObject("author_id", UUID.class), postId).stream().findFirst();
    }

    public Optional<UUID> commentAuthor(UUID commentId) {
        return jdbcTemplate.query("SELECT author_id FROM comments WHERE id = ?", (rs, row) ->
                rs.getObject("author_id", UUID.class), commentId).stream().findFirst();
    }

    public Optional<UUID> commentPost(UUID commentId) {
        return jdbcTemplate.query("SELECT post_id FROM comments WHERE id = ?", (rs, row) ->
                rs.getObject("post_id", UUID.class), commentId).stream().findFirst();
    }

    public List<NotificationResponse> findAll(UUID userId, int page, int size) {
        return jdbcTemplate.query("""
                SELECT n.id, n.type, n.actor_user_id, n.post_id, n.comment_id, n.is_read, n.created_at,
                       p.username, p.display_name, p.avatar_url
                  FROM notifications n
                  LEFT JOIN profiles p ON p.user_id = n.actor_user_id
                 WHERE n.user_id = ?
                 ORDER BY n.created_at DESC, n.id DESC
                 LIMIT ? OFFSET ?
                """, (rs, row) -> {
            UUID actorId = rs.getObject("actor_user_id", UUID.class);
            AuthorSummary actor = actorId == null ? null : new AuthorSummary(
                    actorId, rs.getString("username"), rs.getString("display_name"), rs.getString("avatar_url"));
            return new NotificationResponse(
                    rs.getObject("id", UUID.class), rs.getString("type"), actor,
                    rs.getObject("post_id", UUID.class), rs.getObject("comment_id", UUID.class),
                    rs.getBoolean("is_read"), rs.getObject("created_at", OffsetDateTime.class).toInstant());
        }, userId, size + 1, (long) page * size);
    }

    public long unreadCount(UUID userId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE", Long.class, userId);
        return count == null ? 0 : count;
    }

    public int markRead(UUID notificationId, UUID userId) {
        return jdbcTemplate.update(
                "UPDATE notifications SET is_read = TRUE WHERE id = ? AND user_id = ?", notificationId, userId);
    }

    public void markAllRead(UUID userId) {
        jdbcTemplate.update("UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE", userId);
    }
}
