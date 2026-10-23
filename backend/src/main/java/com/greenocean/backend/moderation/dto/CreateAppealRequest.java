package com.greenocean.backend.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAppealRequest(UUID reportId, @NotBlank @Size(max = 2000) String reason) {}
