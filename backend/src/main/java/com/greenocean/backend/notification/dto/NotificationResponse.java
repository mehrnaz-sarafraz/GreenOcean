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
        boolean read,
        Instant createdAt
) {
}
