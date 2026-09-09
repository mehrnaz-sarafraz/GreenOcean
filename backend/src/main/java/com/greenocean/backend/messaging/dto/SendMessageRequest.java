package com.greenocean.backend.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SendMessageRequest(@NotBlank @Size(max = 5000) String body, UUID clientMessageId) {
}
