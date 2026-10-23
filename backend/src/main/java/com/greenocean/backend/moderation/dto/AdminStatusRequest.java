package com.greenocean.backend.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminStatusRequest(@NotBlank String status, @Size(max = 2000) String note) {}
