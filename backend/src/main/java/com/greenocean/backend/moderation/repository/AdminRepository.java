package com.greenocean.backend.moderation.repository;

import com.greenocean.backend.moderation.dto.AdminDashboardResponse;
import com.greenocean.backend.moderation.dto.AdminOperationsResponse;
import com.greenocean.backend.moderation.dto.BroadcastRequest;
import com.greenocean.backend.moderation.dto.CreateCampaignRequest;
import com.greenocean.backend.moderation.dto.CreatePolicyRequest;
import com.greenocean.backend.moderation.dto.AdminExportFile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Repository
public class AdminRepository {
    private final JdbcTemplate jdbcTemplate;
    public AdminRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public AdminDashboardResponse dashboard() {
        return new AdminDashboardResponse(stats(), trend(), reasons(), reports(), verifications(), members(), audit());
    }

    public AdminOperationsResponse operations() {
        return new AdminOperationsResponse(pendingReviews(), listenerApplications(), campaigns(), policies(), exports(),
                appeals(), communities(), broadcasts(), supportRequests(), emailDeliveries());
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
        if ("APPROVE_VERIFICATION".equals(action)) {
            Long clean = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM professional_application_documents
                     WHERE application_id = ? AND scan_status = 'CLEAN'
                    """, Long.class, verificationId);
            Long total = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM professional_application_documents WHERE application_id = ?
                    """, Long.class, verificationId);
            if (total == null || total == 0 || !total.equals(clean)) {
                throw new IllegalArgumentException("Every uploaded credential document must be reviewed as clean before approval");
            }
        }
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
            if ("APPROVED".equals(status)) {
                jdbcTemplate.update("""
                        INSERT INTO user_roles (user_id, role_id)
                        SELECT pv.professional_user_id, r.id FROM professional_verifications pv
                          JOIN roles r ON r.name = 'PROFESSIONAL' WHERE pv.id = ?
                        ON CONFLICT DO NOTHING
                        """, verificationId);
            }
        }
        if (updated == 1) audit(actorId, "VERIFICATION_" + action, "PROFESSIONAL_VERIFICATION", verificationId);
        return updated == 1;
    }

    public boolean updateReview(UUID id, UUID actorId, String action, String note) {
        String status = switch (action) {
            case "APPROVE_REVIEW" -> "PUBLISHED";
            case "REJECT_REVIEW" -> "REJECTED";
            default -> throw new IllegalArgumentException("Unsupported review action");
        };
        int updated = jdbcTemplate.update("UPDATE professional_reviews SET status = ? WHERE id = ?", status, id);
        if (updated == 1) {
            jdbcTemplate.update("""
                    UPDATE professional_profiles
                       SET rating = COALESCE((SELECT AVG(rating) FROM professional_reviews
                                              WHERE professional_user_id = professional_profiles.user_id
                                                AND status = 'PUBLISHED'), 0),
                           review_count = (SELECT COUNT(*) FROM professional_reviews
                                            WHERE professional_user_id = professional_profiles.user_id
                                              AND status = 'PUBLISHED')
                     WHERE user_id = (SELECT professional_user_id FROM professional_reviews WHERE id = ?)
                    """, id);
            audit(actorId, action, "PROFESSIONAL_REVIEW", id);
        }
        return updated == 1;
    }

    public boolean updateListenerApplication(UUID id, UUID actorId, String action, String note) {
        if ("APPROVE_LISTENER".equals(action)) {
            Long completed = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM listener_training_progress
                     WHERE user_id = (SELECT user_id FROM listener_applications WHERE id = ?)
                    """, Long.class, id);
            if (completed == null || completed < 5) throw new IllegalArgumentException("Listener coursework is incomplete");
        }
        String status = switch (action) {
            case "APPROVE_LISTENER" -> "APPROVED";
            case "REJECT_LISTENER" -> "REJECTED";
            case "REVIEW_LISTENER" -> "IN_REVIEW";
            default -> throw new IllegalArgumentException("Unsupported listener application action");
        };
        int updated = jdbcTemplate.update("""
                UPDATE listener_applications SET status = ?, reviewed_by = ?, review_note = ?,
                    reviewed_at = CASE WHEN ? IN ('APPROVED','REJECTED') THEN CURRENT_TIMESTAMP ELSE NULL END
                 WHERE id = ?
                """, status, actorId, normalize(note), status, id);
        if (updated == 1 && "APPROVED".equals(status)) {
            jdbcTemplate.update("""
                    INSERT INTO listener_profiles (user_id, listening_style, languages, topics)
                    SELECT user_id, 'Supportive and reflective', ARRAY['English'], ARRAY['General support']
                      FROM listener_applications WHERE id = ? ON CONFLICT (user_id) DO UPDATE SET status = 'ACTIVE'
                    """, id);
        }
        if (updated == 1) audit(actorId, action, "LISTENER_APPLICATION", id);
        return updated == 1;
    }

    public boolean updateCampaign(UUID id, UUID actorId, String action) {
        String status = switch (action) {
            case "START_CAMPAIGN" -> "ACTIVE";
            case "PAUSE_CAMPAIGN" -> "PAUSED";
            case "COMPLETE_CAMPAIGN" -> "COMPLETED";
            default -> throw new IllegalArgumentException("Unsupported campaign action");
        };
        int updated = jdbcTemplate.update("UPDATE promotion_campaigns SET status = ? WHERE id = ?", status, id);
        if (updated == 1) audit(actorId, action, "PROMOTION_CAMPAIGN", id);
        return updated == 1;
    }

    public boolean updateAppeal(UUID id, UUID actorId, String action, String note) {
        String status = switch (action) {
            case "REVIEW_APPEAL" -> "REVIEWING";
            case "UPHOLD_APPEAL" -> "UPHELD";
            case "OVERTURN_APPEAL" -> "OVERTURNED";
            default -> throw new IllegalArgumentException("Unsupported appeal action");
        };
        int updated = jdbcTemplate.update("""
                UPDATE moderation_appeals SET status = ?, decision_note = ?, decided_by = ?,
                       decided_at = CASE WHEN ? IN ('UPHELD','OVERTURNED') THEN CURRENT_TIMESTAMP ELSE NULL END
                 WHERE id = ?
                """, status, normalize(note), actorId, status, id);
        if (updated == 1) audit(actorId, action, "MODERATION_APPEAL", id);
        return updated == 1;
    }

    public UUID createBroadcast(UUID id, UUID actorId, BroadcastRequest request) {
        jdbcTemplate.update("""
                INSERT INTO broadcast_notices (id, created_by, title, body, audience, category)
                VALUES (?, ?, ?, ?, ?, ?)
                """, id, actorId, request.title().trim(), request.body().trim(), request.audience(), request.category());
        String audience = switch (request.audience()) {
            case "PROFESSIONALS" -> "EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id = ur.role_id WHERE ur.user_id = u.id AND r.name = 'PROFESSIONAL')";
            case "LISTENERS" -> "EXISTS(SELECT 1 FROM listener_profiles lp WHERE lp.user_id = u.id AND lp.status = 'ACTIVE')";
            case "ALL", "MEMBERS" -> "TRUE";
            default -> throw new IllegalArgumentException("Unsupported broadcast audience");
        };
        String consent = switch (request.category()) {
            case "COMMUNITY" -> "COALESCE(up.notify_groups, TRUE)";
            case "EDITORIAL" -> "COALESCE(up.notify_articles, TRUE)";
            case "SAFETY", "SERVICE" -> "TRUE";
            default -> throw new IllegalArgumentException("Unsupported broadcast category");
        };
        int recipients = jdbcTemplate.update("""
                INSERT INTO notifications (user_id, type, title, body, broadcast_id)
                SELECT u.id, 'SYSTEM_NOTICE', ?, ?, ? FROM users u
                  LEFT JOIN user_preferences up ON up.user_id = u.id
                 WHERE u.status = 'ACTIVE' AND u.deleted_at IS NULL AND """ + audience + " AND " + consent,
                request.title().trim(), request.body().trim(), id);
        jdbcTemplate.update("""
                UPDATE broadcast_notices SET status = 'SENT', recipient_count = ?, sent_at = CURRENT_TIMESTAMP WHERE id = ?
                """, recipients, id);
        audit(actorId, "BROADCAST_SENT", "BROADCAST_NOTICE", id);
        return id;
    }

    public UUID createExport(UUID id, UUID actorId, String type) {
        long rows = switch (type) {
            case "MEMBERS" -> jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
            case "REPORTS" -> jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reports", Long.class);
            case "PROFESSIONALS" -> jdbcTemplate.queryForObject("SELECT COUNT(*) FROM professional_profiles", Long.class);
            case "ACTIVITY" -> jdbcTemplate.queryForObject("SELECT COUNT(*) FROM audit_logs", Long.class);
            default -> throw new IllegalArgumentException("Unsupported export type");
        };
        jdbcTemplate.update("""
                INSERT INTO export_jobs (id, requested_by, export_type, status, row_count, expires_at, completed_at)
                VALUES (?, ?, ?, 'COMPLETED', ?, CURRENT_TIMESTAMP + INTERVAL '7 days', CURRENT_TIMESTAMP)
                """, id, actorId, type, rows);
        audit(actorId, "EXPORT_CREATED", "EXPORT_JOB", id);
        return id;
    }

    public UUID createCampaign(UUID id, UUID actorId, CreateCampaignRequest request) {
        if (!request.endsAt().isAfter(request.startsAt())) throw new IllegalArgumentException("Campaign end must follow its start");
        int created = jdbcTemplate.update("""
                INSERT INTO promotion_campaigns (id, professional_user_id, title, starts_at, ends_at, status)
                SELECT ?, pp.user_id, ?, ?, ?, 'SCHEDULED' FROM professional_profiles pp
                 WHERE pp.user_id = ? AND pp.verification_status = 'VERIFIED'
                """, id, request.title().trim(), request.startsAt(), request.endsAt(), request.professionalId());
        if (created == 0) throw new IllegalArgumentException("Campaign professional must be verified");
        audit(actorId, "CAMPAIGN_CREATED", "PROMOTION_CAMPAIGN", id);
        return id;
    }

    public Optional<AdminExportFile> downloadExport(UUID id, UUID actorId) {
        List<String> types = jdbcTemplate.query("""
                SELECT export_type FROM export_jobs
                 WHERE id = ? AND status = 'COMPLETED' AND expires_at > CURRENT_TIMESTAMP
                """, (rs, row) -> rs.getString("export_type"), id);
        if (types.isEmpty()) return Optional.empty();
        String type = types.getFirst();
        List<String> headers;
        List<Map<String, Object>> rows;
        switch (type) {
            case "MEMBERS" -> {
                headers = List.of("id", "username", "display_name", "status", "email_verified", "created_at", "post_count", "report_count");
                rows = jdbcTemplate.queryForList("""
                        SELECT u.id, p.username, p.display_name, u.status, u.email_verified, u.created_at,
                               (SELECT COUNT(*) FROM posts po WHERE po.author_id = u.id) AS post_count,
                               (SELECT COUNT(*) FROM reports r WHERE r.target_user_id = u.id) AS report_count
                          FROM users u JOIN profiles p ON p.user_id = u.id
                         ORDER BY u.created_at
                        """);
            }
            case "REPORTS" -> {
                headers = List.of("id", "reason", "status", "severity", "target_type", "created_at", "resolved_at");
                rows = jdbcTemplate.queryForList("""
                        SELECT id, reason, status, severity,
                               CASE WHEN target_user_id IS NOT NULL THEN 'PROFILE'
                                    WHEN post_id IS NOT NULL THEN 'POST'
                                    WHEN comment_id IS NOT NULL THEN 'COMMENT'
                                    WHEN conversation_id IS NOT NULL THEN 'CONVERSATION' ELSE 'COMMUNITY' END AS target_type,
                               created_at, resolved_at
                          FROM reports ORDER BY created_at
                        """);
            }
            case "PROFESSIONALS" -> {
                headers = List.of("id", "username", "display_name", "professional_type", "title", "country", "city", "verification_status", "go_score", "rating", "review_count", "verified_at");
                rows = jdbcTemplate.queryForList("""
                        SELECT pp.user_id AS id, p.username, p.display_name, pp.professional_type, pp.title,
                               pp.country, pp.city, pp.verification_status, pp.go_score, pp.rating,
                               pp.review_count, pp.verified_at
                          FROM professional_profiles pp JOIN profiles p ON p.user_id = pp.user_id
                         ORDER BY p.display_name
                        """);
            }
            case "ACTIVITY" -> {
                headers = List.of("id", "actor_id", "action", "target_type", "target_id", "created_at");
                rows = jdbcTemplate.queryForList("""
                        SELECT id, actor_user_id AS actor_id, action, entity_type AS target_type,
                               entity_id AS target_id, created_at
                          FROM audit_logs ORDER BY created_at
                        """);
            }
            default -> throw new IllegalArgumentException("Unsupported export type");
        }
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append(String.join(",", headers)).append("\r\n");
        for (Map<String, Object> row : rows) {
            csv.append(headers.stream().map(header -> csvCell(row.get(header))).collect(Collectors.joining(",")))
                    .append("\r\n");
        }
        audit(actorId, "EXPORT_DOWNLOADED", "EXPORT_JOB", id);
        String filename = "greenocean-" + type.toLowerCase() + "-" + id + ".csv";
        return Optional.of(new AdminExportFile(filename, csv.toString().getBytes(StandardCharsets.UTF_8)));
    }

    public UUID createPolicy(UUID id, UUID actorId, CreatePolicyRequest request) {
        String key = request.key().trim().toLowerCase().replaceAll("[^a-z0-9-]+", "-");
        jdbcTemplate.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,0))","policy:"+key);
        Integer nextVersion = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(version), 0) + 1 FROM policy_documents WHERE policy_key = ?", Integer.class, key);
        if (Boolean.TRUE.equals(request.publish())) throw new com.greenocean.backend.common.exception.ForbiddenException("Create an unpublished draft first");
        jdbcTemplate.update("""
                INSERT INTO policy_documents
                    (id, policy_key, version, title, summary, content, status, published_at, updated_by,locale,effective_at,review_status)
                VALUES (?, ?, ?, ?, ?, ?, 'DRAFT', NULL, ?, ?, ?, 'DRAFT')
                """, id, key, nextVersion, request.title().trim(), request.summary().trim(), request.content().trim(),
                actorId, request.locale()==null?"en":request.locale(), request.effectiveAt()==null?null:java.sql.Timestamp.from(request.effectiveAt()));
        audit(actorId, "POLICY_CREATED", "POLICY_DOCUMENT", id);
        return id;
    }

    public boolean updateUserStatus(UUID id, UUID actorId, String status, String note) {
        if (id.equals(actorId)) throw new IllegalArgumentException("You cannot change your own account status here");
        int updated = jdbcTemplate.update("""
                UPDATE users SET status = ?, updated_at = CURRENT_TIMESTAMP
                 WHERE id = ? AND status <> 'DELETED'
                """, status, id);
        if (updated == 1) {
            if (!"ACTIVE".equals(status)) jdbcTemplate.update("UPDATE user_sessions SET revoked_at = CURRENT_TIMESTAMP WHERE user_id = ? AND revoked_at IS NULL", id);
            audit(actorId, "USER_" + status + (normalize(note) == null ? "" : "_WITH_NOTE"), "USER", id);
        }
        return updated == 1;
    }

    public boolean updateCommunityStatus(UUID id, UUID actorId, String status, String note) {
        int updated = jdbcTemplate.update("UPDATE communities SET status = ? WHERE id = ?", status, id);
        if (updated == 1) audit(actorId, "COMMUNITY_" + status + (normalize(note) == null ? "" : "_WITH_NOTE"), "COMMUNITY", id);
        return updated == 1;
    }

    public boolean updateDocumentScan(UUID id, UUID actorId, String status) {
        int updated = jdbcTemplate.update("""
                UPDATE professional_application_documents
                   SET scan_status = ?, scanned_at = CURRENT_TIMESTAMP, reviewed_by = ?
                 WHERE id = ?
                """, status, actorId, id);
        if (updated == 1) audit(actorId, "CREDENTIAL_DOCUMENT_" + status, "PROFESSIONAL_DOCUMENT", id);
        return updated == 1;
    }

    public boolean updateSupportRequest(UUID id, UUID actorId, String status, String note) {
        int updated = jdbcTemplate.update("""
                UPDATE support_requests SET status = ?, assigned_to = ?, resolution_note = ?,
                       resolved_at = CASE WHEN ? IN ('RESOLVED','CLOSED') THEN CURRENT_TIMESTAMP ELSE NULL END
                 WHERE id = ?
                """, status, actorId, normalize(note), status, id);
        if (updated == 1) audit(actorId, "SUPPORT_" + status, "SUPPORT_REQUEST", id);
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
                       COALESCE(target_profile.username, post_profile.username, comment_profile.username, community.name, 'private conversation') AS reported_username,
                       CASE WHEN r.target_user_id IS NOT NULL THEN 'PROFILE'
                            WHEN r.post_id IS NOT NULL THEN 'POST'
                            WHEN r.comment_id IS NOT NULL THEN 'COMMENT'
                            WHEN r.conversation_id IS NOT NULL THEN 'CONVERSATION' ELSE 'COMMUNITY' END AS target_type
                  FROM reports r
                  JOIN profiles reporter ON reporter.user_id = r.reporter_id
                  LEFT JOIN profiles target_profile ON target_profile.user_id = r.target_user_id
                  LEFT JOIN posts target_post ON target_post.id = r.post_id
                  LEFT JOIN profiles post_profile ON post_profile.user_id = target_post.author_id
                  LEFT JOIN comments target_comment ON target_comment.id = r.comment_id
                  LEFT JOIN profiles comment_profile ON comment_profile.user_id = target_comment.author_id
                  LEFT JOIN communities community ON community.id = r.community_id
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
                verificationDocuments(rs.getObject("id", UUID.class))));
    }

    private List<AdminDashboardResponse.VerificationDocumentItem> verificationDocuments(UUID applicationId) {
        return jdbcTemplate.query("""
                SELECT id, original_name, content_type, byte_size, sha256, scan_status, uploaded_at
                  FROM professional_application_documents WHERE application_id = ? ORDER BY uploaded_at
                """, (rs, row) -> new AdminDashboardResponse.VerificationDocumentItem(
                rs.getObject("id", UUID.class), rs.getString("original_name"), rs.getString("content_type"),
                rs.getLong("byte_size"), rs.getString("sha256"), rs.getString("scan_status"),
                rs.getObject("uploaded_at", OffsetDateTime.class).toInstant()), applicationId);
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

    private List<AdminOperationsResponse.ReviewItem> pendingReviews() {
        return jdbcTemplate.query("""
                SELECT r.id, r.professional_user_id, professional.display_name AS professional_name,
                       reviewer.display_name AS reviewer_name, r.rating, r.body, r.status, r.created_at
                  FROM professional_reviews r
                  JOIN profiles professional ON professional.user_id = r.professional_user_id
                  JOIN profiles reviewer ON reviewer.user_id = r.reviewer_user_id
                 ORDER BY CASE r.status WHEN 'PENDING' THEN 0 ELSE 1 END, r.created_at DESC
                """, (rs, row) -> new AdminOperationsResponse.ReviewItem(rs.getObject("id", UUID.class),
                rs.getObject("professional_user_id", UUID.class), rs.getString("professional_name"),
                rs.getString("reviewer_name"), rs.getInt("rating"), rs.getString("body"), rs.getString("status"),
                rs.getObject("created_at", OffsetDateTime.class).toInstant()));
    }

    private List<AdminOperationsResponse.ListenerApplicationItem> listenerApplications() {
        return jdbcTemplate.query("""
                SELECT a.id, a.user_id, p.display_name, a.motivation, a.experience, a.weekly_availability,
                       a.status, a.submitted_at FROM listener_applications a JOIN profiles p ON p.user_id = a.user_id
                 ORDER BY CASE a.status WHEN 'SUBMITTED' THEN 0 WHEN 'IN_REVIEW' THEN 1 ELSE 2 END, a.submitted_at
                """, (rs, row) -> new AdminOperationsResponse.ListenerApplicationItem(rs.getObject("id", UUID.class),
                rs.getObject("user_id", UUID.class), rs.getString("display_name"), rs.getString("motivation"),
                rs.getString("experience"), rs.getString("weekly_availability"), rs.getString("status"),
                rs.getObject("submitted_at", OffsetDateTime.class).toInstant()));
    }

    private List<AdminOperationsResponse.CampaignItem> campaigns() {
        return jdbcTemplate.query("""
                SELECT c.*, p.display_name FROM promotion_campaigns c JOIN profiles p ON p.user_id = c.professional_user_id
                 ORDER BY c.starts_at DESC
                """, (rs, row) -> new AdminOperationsResponse.CampaignItem(rs.getObject("id", UUID.class),
                rs.getObject("professional_user_id", UUID.class), rs.getString("display_name"), rs.getString("title"),
                rs.getObject("starts_at", OffsetDateTime.class).toInstant(), rs.getObject("ends_at", OffsetDateTime.class).toInstant(),
                rs.getString("status"), rs.getLong("impressions"), rs.getLong("clicks")));
    }

    private List<AdminOperationsResponse.PolicyItem> policies() {
        return jdbcTemplate.query("SELECT * FROM policy_documents ORDER BY policy_key, version DESC", (rs, row) -> {
            OffsetDateTime published = rs.getObject("published_at", OffsetDateTime.class);
            return new AdminOperationsResponse.PolicyItem(rs.getObject("id", UUID.class), rs.getString("policy_key"),
                    rs.getInt("version"), rs.getString("title"), rs.getString("summary"), rs.getString("status"),
                    published == null ? null : published.toInstant());
        });
    }

    private List<AdminOperationsResponse.ExportItem> exports() {
        return jdbcTemplate.query("""
                SELECT e.*, COALESCE(p.display_name, 'Unknown') AS requester FROM export_jobs e
                  LEFT JOIN profiles p ON p.user_id = e.requested_by ORDER BY e.created_at DESC LIMIT 100
                """, (rs, row) -> {
            OffsetDateTime completed = rs.getObject("completed_at", OffsetDateTime.class);
            Number count = (Number) rs.getObject("row_count");
            return new AdminOperationsResponse.ExportItem(rs.getObject("id", UUID.class), rs.getString("export_type"),
                    rs.getString("requester"), rs.getString("status"), count == null ? null : count.longValue(),
                    rs.getObject("created_at", OffsetDateTime.class).toInstant(),
                    completed == null ? null : completed.toInstant());
        });
    }

    private List<AdminOperationsResponse.AppealItem> appeals() {
        return jdbcTemplate.query("""
                SELECT a.*, p.display_name FROM moderation_appeals a
                  JOIN profiles p ON p.user_id = a.appellant_user_id
                 ORDER BY CASE a.status WHEN 'OPEN' THEN 0 WHEN 'REVIEWING' THEN 1 ELSE 2 END, a.created_at
                """, (rs, row) -> {
            OffsetDateTime decided = rs.getObject("decided_at", OffsetDateTime.class);
            return new AdminOperationsResponse.AppealItem(rs.getObject("id", UUID.class),
                    rs.getObject("appellant_user_id", UUID.class), rs.getString("display_name"),
                    rs.getObject("report_id", UUID.class), rs.getString("reason"), rs.getString("status"),
                    rs.getString("decision_note"), rs.getObject("created_at", OffsetDateTime.class).toInstant(),
                    decided == null ? null : decided.toInstant());
        });
    }

    private List<AdminOperationsResponse.CommunityItem> communities() {
        return jdbcTemplate.query("""
                SELECT c.id, c.name, c.slug, c.status,
                       COUNT(DISTINCT m.user_id) AS members,
                       COUNT(DISTINCT m.user_id) FILTER (WHERE m.role IN ('OWNER','MODERATOR')) AS moderators,
                       COUNT(DISTINCT p.id) FILTER (WHERE p.deleted_at IS NULL) AS posts,
                       COUNT(DISTINCT r.id) FILTER (WHERE r.status IN ('PENDING','IN_REVIEW')) AS open_reports
                  FROM communities c
                  LEFT JOIN community_members m ON m.community_id = c.id
                  LEFT JOIN posts p ON p.community_id = c.id
                  LEFT JOIN reports r ON r.community_id = c.id
                 GROUP BY c.id ORDER BY open_reports DESC, members DESC
                """, (rs, row) -> new AdminOperationsResponse.CommunityItem(rs.getObject("id", UUID.class),
                rs.getString("name"), rs.getString("slug"), rs.getLong("members"), rs.getLong("moderators"),
                rs.getLong("posts"), rs.getLong("open_reports"), rs.getString("status")));
    }

    private List<AdminOperationsResponse.BroadcastItem> broadcasts() {
        return jdbcTemplate.query("SELECT * FROM broadcast_notices ORDER BY created_at DESC LIMIT 100", (rs, row) -> {
            OffsetDateTime sent = rs.getObject("sent_at", OffsetDateTime.class);
            return new AdminOperationsResponse.BroadcastItem(rs.getObject("id", UUID.class), rs.getString("title"),
                    rs.getString("body"), rs.getString("audience"), rs.getString("category"), rs.getString("status"),
                    rs.getLong("recipient_count"), rs.getObject("created_at", OffsetDateTime.class).toInstant(),
                    sent == null ? null : sent.toInstant());
        });
    }

    private List<AdminOperationsResponse.SupportRequestItem> supportRequests() {
        return jdbcTemplate.query("""
                SELECT id, user_id, email, message, status, resolution_note, created_at, resolved_at
                  FROM support_requests
                 ORDER BY CASE status WHEN 'OPEN' THEN 0 WHEN 'IN_PROGRESS' THEN 1 ELSE 2 END, created_at
                 LIMIT 200
                """, (rs, row) -> {
            OffsetDateTime resolved = rs.getObject("resolved_at", OffsetDateTime.class);
            return new AdminOperationsResponse.SupportRequestItem(rs.getObject("id", UUID.class),
                    rs.getObject("user_id", UUID.class), rs.getString("email"), rs.getString("message"),
                    rs.getString("status"), rs.getString("resolution_note"),
                    rs.getObject("created_at", OffsetDateTime.class).toInstant(),
                    resolved == null ? null : resolved.toInstant());
        });
    }

    private List<AdminOperationsResponse.EmailDeliveryItem> emailDeliveries() {
        return jdbcTemplate.query("""
                SELECT id, recipient, template_key, status, attempts, created_at, sent_at
                  FROM email_outbox ORDER BY created_at DESC LIMIT 100
                """, (rs, row) -> {
            OffsetDateTime sent = rs.getObject("sent_at", OffsetDateTime.class);
            return new AdminOperationsResponse.EmailDeliveryItem(rs.getObject("id", UUID.class),
                    rs.getString("recipient"), rs.getString("template_key"), rs.getString("status"),
                    rs.getInt("attempts"), rs.getObject("created_at", OffsetDateTime.class).toInstant(),
                    sent == null ? null : sent.toInstant());
        });
    }

    private void audit(UUID actorId, String action, String entityType, UUID entityId) {
        jdbcTemplate.update("INSERT INTO audit_logs (actor_user_id, action, entity_type, entity_id) VALUES (?, ?, ?, ?)",
                actorId, action, entityType, entityId);
    }
    private String status(String dbStatus) {
        return switch (dbStatus) { case "PENDING" -> "OPEN"; case "IN_REVIEW" -> "REVIEWING"; default -> "RESOLVED"; };
    }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String csvCell(Object raw) {
        String value = raw == null ? "" : raw.toString();
        if (!value.isEmpty() && "=+-@".indexOf(value.charAt(0)) >= 0) value = "'" + value;
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    private long number(Map<String, Object> row, String key) { return ((Number) row.get(key)).longValue(); }
    private List<String> strings(Array array) throws SQLException {
        return array == null ? List.of() : Arrays.asList((String[]) array.getArray());
    }
}
