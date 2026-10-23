package com.greenocean.backend.moderation.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminOperationsResponse(
        List<ReviewItem> pendingReviews,
        List<ListenerApplicationItem> listenerApplications,
        List<CampaignItem> campaigns,
        List<PolicyItem> policies,
        List<ExportItem> exports,
        List<AppealItem> appeals,
        List<CommunityItem> communities,
        List<BroadcastItem> broadcasts,
        List<SupportRequestItem> supportRequests,
        List<EmailDeliveryItem> emailDeliveries
) {
    public record ReviewItem(UUID id, UUID professionalId, String professionalName, String reviewerName,
                             int rating, String body, String status, Instant createdAt) {}
    public record ListenerApplicationItem(UUID id, UUID userId, String name, String motivation, String experience,
                                          String weeklyAvailability, String status, Instant submittedAt) {}
    public record CampaignItem(UUID id, UUID professionalId, String professionalName, String title,
                               Instant startsAt, Instant endsAt, String status, long impressions, long clicks) {}
    public record PolicyItem(UUID id, String key, int version, String title, String summary,
                             String status, Instant publishedAt) {}
    public record ExportItem(UUID id, String type, String requester, String status, Long rowCount,
                             Instant createdAt, Instant completedAt) {}
    public record AppealItem(UUID id, UUID appellantId, String appellantName, UUID reportId, String reason,
                             String status, String decisionNote, Instant createdAt, Instant decidedAt) {}
    public record CommunityItem(UUID id, String name, String slug, long members, long moderators,
                                long posts, long openReports, String status) {}
    public record BroadcastItem(UUID id, String title, String body, String audience, String category,
                                String status, long recipientCount, Instant createdAt, Instant sentAt) {}
    public record SupportRequestItem(UUID id, UUID userId, String email, String message, String status,
                                     String resolutionNote, Instant createdAt, Instant resolvedAt) {}
    public record EmailDeliveryItem(UUID id, String recipient, String templateKey, String status,
                                    int attempts, Instant createdAt, Instant sentAt) {}
}
