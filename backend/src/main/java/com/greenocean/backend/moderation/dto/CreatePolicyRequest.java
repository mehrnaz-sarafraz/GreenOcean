package com.greenocean.backend.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePolicyRequest(
        @NotBlank @Size(max = 80) @jakarta.validation.constraints.Pattern(regexp="[a-z0-9]+(?:-[a-z0-9]+)*") String key,
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 1000) String summary,
        @NotBlank @Size(max = 50000) String content,
        Boolean publish,
        @jakarta.validation.constraints.Pattern(regexp="en|fa") String locale,
        java.time.Instant effectiveAt
) {}
