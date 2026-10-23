package com.greenocean.backend.professional.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateAvailabilityRequest(
        @NotNull @Future OffsetDateTime startsAt,
        @NotNull @Future OffsetDateTime endsAt,
        @NotBlank @Size(max = 80) String consultationMode
) {
}
