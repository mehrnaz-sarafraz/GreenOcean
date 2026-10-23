package com.greenocean.backend.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateReportRequest(
        @NotBlank @Pattern(regexp = "USER|POST|COMMENT|CONVERSATION|COMMUNITY") String targetType,
        @NotNull UUID targetId,
        @NotBlank @Pattern(regexp = "HARASSMENT|SPAM|HATE_SPEECH|SEXUAL_CONTENT|SELF_HARM_CONTENT|MISINFORMATION|OTHER") String reason,
        @Size(max = 1000) String description
) {}
