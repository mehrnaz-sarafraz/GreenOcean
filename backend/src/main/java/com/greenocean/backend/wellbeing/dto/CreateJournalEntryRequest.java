package com.greenocean.backend.wellbeing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateJournalEntryRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 20000) String body,
        @Size(max = 40) String mood
) {}
