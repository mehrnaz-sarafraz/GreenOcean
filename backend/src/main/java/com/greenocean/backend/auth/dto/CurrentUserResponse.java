package com.greenocean.backend.auth.dto;

import java.util.List;
import java.util.UUID;

public record CurrentUserResponse(UUID userId, String email, List<String> roles) {
}
