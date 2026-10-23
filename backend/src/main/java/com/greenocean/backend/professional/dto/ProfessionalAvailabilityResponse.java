package com.greenocean.backend.professional.dto;

import java.time.Instant;
import java.util.UUID;

public record ProfessionalAvailabilityResponse(
        UUID id,
        UUID professionalId,
        Instant startsAt,
        Instant endsAt,
        String consultationMode,
        String status
) {
}
