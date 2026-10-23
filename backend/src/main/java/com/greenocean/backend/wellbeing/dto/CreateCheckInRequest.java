package com.greenocean.backend.wellbeing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCheckInRequest(
        @NotBlank @Size(max = 40) String mood,
        @Size(max = 1000) String note
) {}
