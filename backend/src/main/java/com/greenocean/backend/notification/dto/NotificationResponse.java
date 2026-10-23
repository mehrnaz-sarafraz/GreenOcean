package com.greenocean.backend.notification.dto;

import com.greenocean.backend.post.dto.AuthorSummary;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        AuthorSummary actor,
        UUID postId,
        UUID commentId,
        String title,
        String body,
        boolean read,
        Instant createdAt
) {
}
