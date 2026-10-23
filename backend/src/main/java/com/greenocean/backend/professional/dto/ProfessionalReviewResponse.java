package com.greenocean.backend.professional.dto;

import java.time.Instant;
import java.util.UUID;

public record ProfessionalReviewResponse(
        UUID id, UUID professionalId, String reviewerName, int rating, String body,
        String status, boolean mine, Instant createdAt
) {}
