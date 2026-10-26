package com.greenocean.backend.auth.dto;

import java.util.UUID;

public record RegisterResponse(String message) {
    public static RegisterResponse pending() {
        return new RegisterResponse("If registration can proceed, check your email for verification. You can also sign in or use account recovery.");
    }
}
