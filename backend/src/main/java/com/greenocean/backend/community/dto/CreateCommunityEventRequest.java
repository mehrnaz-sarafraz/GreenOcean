package com.greenocean.backend.community.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateCommunityEventRequest(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 1000) String description,
        @NotBlank String format,
        @NotNull @Future Instant startsAt,
        @NotNull @Future Instant endsAt,
        @Positive Integer capacity
) {
}
