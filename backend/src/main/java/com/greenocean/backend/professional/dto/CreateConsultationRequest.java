package com.greenocean.backend.professional.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateConsultationRequest(
        UUID availabilitySlotId,
        @Future OffsetDateTime scheduledAt,
        @Size(max = 80) String consultationMode,
        @Size(max = 1000) String note
) {}
