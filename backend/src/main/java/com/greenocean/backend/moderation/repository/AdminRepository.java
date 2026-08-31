package com.greenocean.backend.moderation.repository;

import com.greenocean.backend.moderation.dto.AdminDashboardResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class AdminRepository {
    private final JdbcTemplate jdbcTemplate;
    public AdminRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public AdminDashboardResponse dashboard() {
        return new AdminDashboardResponse(stats(), trend(), reasons(), reports(), verifications(), members(), audit());
    }

    public boolean updateReport(UUID reportId, UUID actorId, String action, String note) {
        String status = switch (action) {
            case "APPROVE" -> "DISMISSED";
            case "REVIEW" -> "IN_REVIEW";
            case "REMOVE" -> "RESOLVED";
            default -> throw new IllegalArgumentException("Unsupported report action");
        };
        int updated = jdbcTemplate.update("""
                UPDATE reports
                   SET status = ?, assigned_to = ?, resolution_note = ?,
                       resolved_at = CASE WHEN ? IN ('RESOLVED', 'DISMISSED') THEN CURRENT_TIMESTAMP ELSE NULL END
                 WHERE id = ?
                """, status, actorId, normalize(note), status, reportId);
        if (updated == 1) audit(actorId, "REPORT_" + action, "REPORT", reportId);
        return updated == 1;
    }

    public boolean updateVerification(UUID verificationId, UUID actorId, String action, String note) {
        String status = switch (action) {
            case "APPROVE_VERIFICATION" -> "APPROVED";
            case "REJECT_VERIFICATION" -> "REJECTED";
            case "REQUEST_INFO" -> "PENDING";
            default -> throw new IllegalArgumentException("Unsupported verification action");
        };
        int updated = jdbcTemplate.update("""
                UPDATE professional_verifications
                   SET status = ?, reviewed_by = CASE WHEN ? = 'PENDING' THEN NULL ELSE ? END,
                       reviewed_at = CASE WHEN ? = 'PENDING' THEN NULL ELSE CURRENT_TIMESTAMP END,
                       notes = ?
                 WHERE id = ?
                """, status, status, actorId, status, normalize(note), verificationId);
        if (updated == 1 && !"PENDING".equals(status)) {
            jdbcTemplate.update("""
                    UPDATE professional_profiles pp
                       SET verification_status = ?, verified_at = CASE WHEN ? = 'VERIFIED' THEN CURRENT_TIMESTAMP ELSE NULL END
                      FROM professional_verifications pv
                     WHERE pv.id = ? AND pp.user_id = pv.professional_user_id
                    """, "APPROVED".equals(status) ? "VERIFIED" : "REJECTED",
                    "APPROVED".equals(status) ? "VERIFIED" : "REJECTED", verificationId);
        }
        if (updated == 1) audit(actorId, "VERIFICATION_" + action, "PROFESSIONAL_VERIFICATION", verificationId);
        return updated == 1;
    }

    private AdminDashboardResponse.Stats stats() {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT (SELECT COUNT(*) FROM users WHERE deleted_at IS NULL) AS members,
                       (SELECT COUNT(*) FROM users WHERE last_login_at >= CURRENT_TIMESTAMP - INTERVAL '1 day') AS active_today,
                       (SELECT COUNT(*) FROM posts WHERE created_at >= CURRENT_TIMESTAMP - INTERVAL '1 day') AS posts_today,
                       (SELECT COUNT(*) FROM reports WHERE status IN ('PENDING','IN_REVIEW')) AS open_reports,
                       (SELECT COUNT(*) FROM reports WHERE status IN ('PENDING','IN_REVIEW') AND severity = 'CRITICAL') AS critical_reports,
                       (SELECT COUNT(*) FROM professional_profiles WHERE verification_status = 'VERIFIED') AS verified_professionals,
                       (SELECT COUNT(*) FROM professional_verifications WHERE status = 'PENDING') AS pending_verifications,
                       (SELECT COUNT(*) FROM reports WHERE resolved_at >= CURRENT_TIMESTAMP - INTERVAL '7 days') AS resolved_week
                """);
        return new AdminDashboardResponse.Stats(number(row, "members"), number(row, "active_today"),
                number(row, "posts_today"), number(row, "open_reports"), number(row, "critical_reports"),
                number(row, "verified_professionals"), number(row, "pending_verifications"), number(row, "resolved_week"));
    }

    private List<Long> trend() {
        return jdbcTemplate.query("""
                SELECT COUNT(r.id) AS count
                  FROM generate_series(CURRENT_DATE - 6, CURRENT_DATE, INTERVAL '1 day') day
                  LEFT JOIN reports r ON r.created_at >= day AND r.created_at < day + INTERVAL '1 day'
                 GROUP BY day ORDER BY day
                """, (rs, row) -> rs.getLong("count"));
    }

    private List<AdminDashboardResponse.ReasonBreakdown> reasons() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT reason, COUNT(*) AS count FROM reports
                 WHERE created_at >= CURRENT_TIMESTAMP - INTERVAL '30 days'
                 GROUP BY reason ORDER BY count DESC
                """);
        long total = rows.stream().mapToLong(row -> number(row, "count")).sum();
        String[] colors = {"#D56C5C", "#6B6FAE", "#4E8CB8", "#2F927D", "#B77A28"};
        java.util.concurrent.atomic.AtomicInteger index = new java.util.concurrent.atomic.AtomicInteger();
        return rows.stream().map(row -> new AdminDashboardResponse.ReasonBreakdown(
                row.get("reason").toString().replace('_', ' '),
                total == 0 ? 0 : Math.round(number(row, "count") * 100.0 / total),
                colors[index.getAndIncrement() % colors.length])).toList();
    }

    private List<AdminDashboardResponse.ReportItem> reports() {
        return jdbcTemplate.query("""
                SELECT r.*, reporter.display_name AS reporter_name,
                       COALESCE(target_profile.username, post_profile.username, comment_profile.username, 'unknown') AS reported_username,
                       CASE WHEN r.target_user_id IS NOT NULL THEN 'PROFILE'
                            WHEN r.post_id IS NOT NULL THEN 'POST' ELSE 'COMMENT' END AS target_type
                  FROM reports r
                  JOIN profiles reporter ON reporter.user_id = r.reporter_id
                  LEFT JOIN profiles target_profile ON target_profile.user_id = r.target_user_id
                  LEFT JOIN posts target_post ON target_post.id = r.post_id
                  LEFT JOIN profiles post_profile ON post_profile.user_id = target_post.author_id
                  LEFT JOIN comments target_comment ON target_comment.id = r.comment_id
                  LEFT JOIN profiles comment_profile ON comment_profile.user_id = target_comment.author_id
                 ORDER BY CASE r.severity WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 ELSE 4 END,
                          r.created_at
                """, (rs, row) -> new AdminDashboardResponse.ReportItem(
                rs.getObject("id", UUID.class), rs.getString("target_type"), rs.getString("reason").replace('_', ' '),
                rs.getString("description"), rs.getString("reported_username"), rs.getString("reporter_name"),
                rs.getString("severity"), status(rs.getString("status")),
                rs.getObject("created_at", OffsetDateTime.class).toInstant(), "Community safety", strings(rs.getArray("signals"))));
    }

    private List<AdminDashboardResponse.VerificationItem> verifications() {
        return jdbcTemplate.query("""
                SELECT pv.id, pv.professional_user_id, p.display_name, pv.profession, pv.country,
                       pv.submitted_at, pv.status, pv.document_names
                  FROM professional_verifications pv
                  JOIN profiles p ON p.user_id = pv.professional_user_id
                 ORDER BY CASE pv.status WHEN 'PENDING' THEN 0 ELSE 1 END, pv.submitted_at
                """, (rs, row) -> new AdminDashboardResponse.VerificationItem(
                rs.getObject("id", UUID.class), rs.getObject("professional_user_id", UUID.class),
                rs.getString("display_name"), rs.getString("profession"), rs.getString("country"),
                rs.getObject("submitted_at", OffsetDateTime.class).toInstant(), rs.getString("status"),
                strings(rs.getArray("document_names"))));
    }

    private List<AdminDashboardResponse.MemberItem> members() {
        return jdbcTemplate.query("""
                SELECT u.id, p.display_name, p.username, u.status,
                       (SELECT COUNT(*) FROM posts post_count WHERE post_count.author_id = u.id) AS post_count,
                       (SELECT COUNT(*) FROM reports report_count
                         WHERE report_count.target_user_id = u.id
                            OR report_count.post_id IN (SELECT id FROM posts WHERE author_id = u.id)
                            OR report_count.comment_id IN (SELECT id FROM comments WHERE author_id = u.id)) AS report_count
                  FROM users u JOIN profiles p ON p.user_id = u.id
                 ORDER BY u.created_at DESC LIMIT 100
                """, (rs, row) -> new AdminDashboardResponse.MemberItem(
                rs.getObject("id", UUID.class), rs.getString("display_name"), rs.getString("username"),
                rs.getString("status"), rs.getLong("post_count"), rs.getLong("report_count")));
    }

    private List<AdminDashboardResponse.AuditItem> audit() {
        return jdbcTemplate.query("""
                SELECT a.id, a.action, COALESCE(p.display_name, 'System') AS actor,
                       CONCAT(a.entity_type, ' · ', COALESCE(a.entity_id::text, 'n/a')) AS target, a.created_at
                  FROM audit_logs a LEFT JOIN profiles p ON p.user_id = a.actor_user_id
                 ORDER BY a.created_at DESC LIMIT 100
                """, (rs, row) -> new AdminDashboardResponse.AuditItem(
                rs.getObject("id", UUID.class), rs.getString("action"), rs.getString("actor"),
                rs.getString("target"), rs.getObject("created_at", OffsetDateTime.class).toInstant()));
    }

    private void audit(UUID actorId, String action, String entityType, UUID entityId) {
        jdbcTemplate.update("INSERT INTO audit_logs (actor_user_id, action, entity_type, entity_id) VALUES (?, ?, ?, ?)",
                actorId, action, entityType, entityId);
    }
    private String status(String dbStatus) {
        return switch (dbStatus) { case "PENDING" -> "OPEN"; case "IN_REVIEW" -> "REVIEWING"; default -> "RESOLVED"; };
    }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private long number(Map<String, Object> row, String key) { return ((Number) row.get(key)).longValue(); }
    private List<String> strings(Array array) throws SQLException {
        return array == null ? List.of() : Arrays.asList((String[]) array.getArray());
    }
}
