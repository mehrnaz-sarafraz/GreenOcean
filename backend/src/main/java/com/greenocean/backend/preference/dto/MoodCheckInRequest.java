package com.greenocean.backend.preference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MoodCheckInRequest(
        @NotBlank @Pattern(regexp = "ROUGH|LOW|OKAY|GOOD|CALM") String mood
) {
}
