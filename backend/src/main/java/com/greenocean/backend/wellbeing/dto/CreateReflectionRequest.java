package com.greenocean.backend.wellbeing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateReflectionRequest(
        @NotNull @Size(min = 5, max = 5) List<@jakarta.validation.constraints.Min(0) @jakarta.validation.constraints.Max(4) Integer> answers
) {
}
