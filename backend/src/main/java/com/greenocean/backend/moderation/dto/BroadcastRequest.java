package com.greenocean.backend.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BroadcastRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(min = 10, max = 2000) String body,
        @NotBlank @Pattern(regexp = "ALL|MEMBERS|PROFESSIONALS|LISTENERS") String audience,
        @NotBlank @Pattern(regexp = "SAFETY|SERVICE|COMMUNITY|EDITORIAL") String category
) {}
