package com.greenocean.backend.moderation.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminDashboardResponse(
        Stats stats,
        List<Long> reportTrend,
        List<ReasonBreakdown> reasonBreakdown,
        List<ReportItem> reports,
        List<VerificationItem> verificationQueue,
        List<MemberItem> members,
        List<AuditItem> auditLog
) {
    public record Stats(long members, long activeToday, long postsToday, long openReports, long criticalReports,
                        long verifiedProfessionals, long pendingVerifications, long resolvedWeek) {
    }
    public record ReasonBreakdown(String name, long value, String color) {
    }
    public record ReportItem(UUID id, String targetType, String reason, String summary, String reportedUser,
                             String reporter, String severity, String status, Instant createdAt,
                             String category, List<String> signals) {
    }
    public record VerificationItem(UUID id, UUID professionalId, String name, String profession, String country,
                                   Instant submittedAt, String status, List<String> documents) {
    }
    public record MemberItem(UUID id, String name, String username, String status, long postCount, long reportCount) {
    }
    public record AuditItem(UUID id, String action, String actor, String target, Instant createdAt) {
    }
}
