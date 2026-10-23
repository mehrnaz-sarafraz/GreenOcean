package com.greenocean.backend.professional.dto;

import java.time.Instant;
import java.util.UUID;

public record ConsultationResponse(
        UUID id, UUID professionalId, String professionalName, UUID requesterId, String requesterName, Instant scheduledAt,
        String consultationMode, String note, String status, Instant createdAt
) {}
