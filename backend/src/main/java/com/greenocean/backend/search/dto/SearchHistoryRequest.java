package com.greenocean.backend.search.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchHistoryRequest(
        @NotBlank @Size(min = 2, max = 100) String term
) {
}
