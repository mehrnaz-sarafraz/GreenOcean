package com.greenocean.backend.moderation.repository;

import com.greenocean.backend.moderation.dto.CreateReportRequest;
import com.greenocean.backend.moderation.dto.ReportResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class SafetyRepository {
    private final JdbcTemplate jdbc;
    public SafetyRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public boolean targetExistsAndVisibleTo(UUID reporterId, CreateReportRequest request) {
        if (request.targetUserId() != null) return exists("SELECT COUNT(*) FROM users WHERE id = ? AND deleted_at IS NULL", request.targetUserId());
        if (request.postId() != null) return exists("SELECT COUNT(*) FROM posts WHERE id = ? AND status <> 'DELETED'", request.postId());
        if (request.commentId() != null) return exists("SELECT COUNT(*) FROM comments WHERE id = ? AND status <> 'DELETED'", request.commentId());
        return exists("""
                SELECT COUNT(*) FROM messages m JOIN conversation_members cm ON cm.conversation_id = m.conversation_id
                 WHERE m.id = ? AND cm.user_id = ? AND m.deleted_at IS NULL
                """, request.messageId(), reporterId);
    }

    public void create(UUID id, UUID reporterId, CreateReportRequest request, String severity) {
        jdbc.update("""
                INSERT INTO reports (id, reporter_id, target_user_id, post_id, comment_id, message_id,
                                     reason, description, severity, signals)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,
                        CASE WHEN ? = 'SELF_HARM_CONTENT' THEN ARRAY['priority-safety-review']::TEXT[] ELSE ARRAY[]::TEXT[] END)
                """, id, reporterId, request.targetUserId(), request.postId(), request.commentId(), request.messageId(),
                request.reason(), normalize(request.description()), severity, request.reason());
    }

    public List<ReportResponse> own(UUID reporterId) {
        return jdbc.query("""
                SELECT id, reason, status, created_at,
                       CASE WHEN target_user_id IS NOT NULL THEN 'USER' WHEN post_id IS NOT NULL THEN 'POST'
                            WHEN comment_id IS NOT NULL THEN 'COMMENT' ELSE 'MESSAGE' END AS target_type
                  FROM reports WHERE reporter_id = ? ORDER BY created_at DESC LIMIT 100
                """, (rs, row) -> new ReportResponse(rs.getObject("id", UUID.class), rs.getString("target_type"),
                rs.getString("reason"), rs.getString("status"), rs.getObject("created_at", OffsetDateTime.class).toInstant()), reporterId);
    }

    public void block(UUID blockerId, UUID blockedId) {
        jdbc.update("INSERT INTO blocks (blocker_id, blocked_id) VALUES (?, ?) ON CONFLICT DO NOTHING", blockerId, blockedId);
    }

    public void unblock(UUID blockerId, UUID blockedId) {
        jdbc.update("DELETE FROM blocks WHERE blocker_id = ? AND blocked_id = ?", blockerId, blockedId);
    }

    private boolean exists(String sql, Object... args) { return jdbc.queryForObject(sql, Long.class, args) > 0; }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
