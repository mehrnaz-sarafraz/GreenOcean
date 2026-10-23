package com.greenocean.backend.wellbeing.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateGrowthPathProgressRequest(
        @NotNull @Min(0) @Max(100) Integer progress
) {}
