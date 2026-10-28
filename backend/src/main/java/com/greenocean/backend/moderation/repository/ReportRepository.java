package com.greenocean.backend.moderation.repository;

import com.greenocean.backend.moderation.dto.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class ReportRepository {
    private final JdbcTemplate jdbcTemplate;
    public ReportRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public void limitSubmissions(UUID user) {
        jdbcTemplate.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,0))","safety-submissions:"+user);
        Long count=jdbcTemplate.queryForObject("SELECT (SELECT count(*) FROM reports WHERE reporter_id=? AND created_at>CURRENT_TIMESTAMP-INTERVAL '1 hour')+(SELECT count(*) FROM moderation_appeals WHERE appellant_user_id=? AND created_at>CURRENT_TIMESTAMP-INTERVAL '1 hour')",Long.class,user,user);
        if(count!=null && count>=20)throw new com.greenocean.backend.common.exception.ConflictException("Please pause before submitting more requests");
    }

    public void lockOpenReport(UUID reporterId, String type, UUID targetId) {
        // Same reporter/target across devices and backend instances; released at transaction end.
        jdbcTemplate.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,0))",
                "open-report:" + reporterId + ":" + type + ":" + targetId);
    }

    public boolean targetExists(String type, UUID id, UUID userId) {
        String sql = switch (type) {
            case "USER" -> "SELECT EXISTS(SELECT 1 FROM users WHERE id = ? AND deleted_at IS NULL)";
            case "POST" -> "SELECT EXISTS(SELECT 1 FROM posts WHERE id = ? AND deleted_at IS NULL AND can_read_social_post(id,?))";
            case "COMMENT" -> "SELECT EXISTS(SELECT 1 FROM comments WHERE id = ? AND deleted_at IS NULL AND can_read_social_comment(id,?))";
            case "CONVERSATION" -> "SELECT EXISTS(SELECT 1 FROM conversation_members WHERE conversation_id = ? AND user_id = ?)";
            case "COMMUNITY" -> "SELECT visible_group(?,?)";
            case "EVENT" -> "SELECT EXISTS(SELECT 1 FROM community_events WHERE id=? AND active_group_member(community_id,?))";
            default -> throw new IllegalArgumentException("Unsupported report target");
        };
        Boolean exists = java.util.Set.of("CONVERSATION","POST","COMMENT","COMMUNITY","EVENT").contains(type)
                ? jdbcTemplate.queryForObject(sql, Boolean.class, id, userId)
                : jdbcTemplate.queryForObject(sql, Boolean.class, id);
        return Boolean.TRUE.equals(exists);
    }

    public boolean duplicateOpenReport(UUID reporterId, String type, UUID targetId) {
        String column = column(type);
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM reports WHERE reporter_id = ? AND " + column + " = ? AND status IN ('PENDING','IN_REVIEW'))",
                Boolean.class, reporterId, targetId));
    }

    public void create(UUID id, UUID reporterId, CreateReportRequest request, String severity, String[] signals) {
        String column = column(request.targetType());
        jdbcTemplate.update("INSERT INTO reports (id, reporter_id, " + column + ", reason, description, severity, signals) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)", id, reporterId, request.targetId(), request.reason(),
                normalize(request.description()), severity, signals);
    }

    public void bindGroupContext(UUID id){jdbcTemplate.update("UPDATE reports r SET context_group_id=COALESCE(r.community_id,(SELECT community_id FROM posts WHERE id=r.post_id),(SELECT p.community_id FROM comments c JOIN posts p ON p.id=c.post_id WHERE c.id=r.comment_id),(SELECT community_id FROM community_events WHERE id=r.event_id)) WHERE r.id=?",id);}

    public List<ReportResponse> mine(UUID userId) {
        return jdbcTemplate.query("""
                SELECT r.*, CASE WHEN target_user_id IS NOT NULL THEN 'USER'
                                 WHEN post_id IS NOT NULL THEN 'POST'
                                 WHEN comment_id IS NOT NULL THEN 'COMMENT'
                                 WHEN conversation_id IS NOT NULL THEN 'CONVERSATION' WHEN event_id IS NOT NULL THEN 'EVENT' ELSE 'COMMUNITY' END AS target_type,
                       COALESCE(target_user_id, post_id, comment_id, conversation_id, community_id,event_id) AS target_id
                  FROM reports r WHERE reporter_id = ? ORDER BY created_at DESC
                """, (rs, row) -> {
            OffsetDateTime resolved = rs.getObject("resolved_at", OffsetDateTime.class);
            return new ReportResponse(rs.getObject("id", UUID.class), rs.getString("target_type"),
                    rs.getObject("target_id", UUID.class), rs.getString("reason"), rs.getString("description"),
                    rs.getString("severity"), rs.getString("status"),
                    rs.getObject("created_at", OffsetDateTime.class).toInstant(),
                    resolved == null ? null : resolved.toInstant(), rs.getString("resolution_note"));
        }, userId);
    }

    public boolean reportBelongsTo(UUID reportId, UUID userId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM reports WHERE id = ? AND reporter_id = ?)", Boolean.class, reportId, userId));
    }

    public void createAppeal(UUID id, UUID userId, CreateAppealRequest request) {
        jdbcTemplate.update("""
                INSERT INTO moderation_appeals (id, appellant_user_id, report_id, reason) VALUES (?, ?, ?, ?)
                """, id, userId, request.reportId(), request.reason().trim());
    }

    public List<AppealResponse> appeals(UUID userId) {
        return jdbcTemplate.query("""
                SELECT id, report_id, reason, status, decision_note, created_at, decided_at
                  FROM moderation_appeals WHERE appellant_user_id = ? ORDER BY created_at DESC
                """, (rs, row) -> {
            OffsetDateTime decided = rs.getObject("decided_at", OffsetDateTime.class);
            return new AppealResponse(rs.getObject("id", UUID.class), rs.getObject("report_id", UUID.class),
                    rs.getString("reason"), rs.getString("status"), rs.getString("decision_note"),
                    rs.getObject("created_at", OffsetDateTime.class).toInstant(),
                    decided == null ? null : decided.toInstant());
        }, userId);
    }

    private String column(String type) {
        return switch (type) {
            case "USER" -> "target_user_id";
            case "POST" -> "post_id";
            case "COMMENT" -> "comment_id";
            case "CONVERSATION" -> "conversation_id";
            case "COMMUNITY" -> "community_id";
            case "EVENT" -> "event_id";
            default -> throw new IllegalArgumentException("Unsupported report target");
        };
    }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
