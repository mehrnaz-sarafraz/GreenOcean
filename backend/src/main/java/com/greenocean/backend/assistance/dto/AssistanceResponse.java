package com.greenocean.backend.assistance.dto;

import java.util.UUID;

public record AssistanceResponse(UUID requestId, String message) {}
