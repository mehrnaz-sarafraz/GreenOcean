package com.greenocean.backend.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateArticleRequest(
        @NotBlank @Size(max = 80) String topic,
        @NotBlank @Size(max = 220) String title,
        @NotBlank @Size(max = 600) String summary,
        @NotBlank @Size(max = 20000) String body,
        @Size(max = 4000) String references,
        boolean pinned
) {
}
