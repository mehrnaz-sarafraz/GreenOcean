package com.greenocean.backend.auth.config;

import com.greenocean.backend.auth.entity.UserStatus;
import com.greenocean.backend.auth.repository.UserSessionRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.time.Instant;

@Component
public class ActiveSessionJwtValidator implements OAuth2TokenValidator<Jwt> {
    private static final OAuth2Error INVALID_SESSION = new OAuth2Error(
            "invalid_token", "The access-token session is no longer active", null);

    private final UserSessionRepository sessionRepository;

    public ActiveSessionJwtValidator(UserSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        try {
            UUID userId = UUID.fromString(jwt.getSubject());
            UUID sessionId = UUID.fromString(jwt.getClaimAsString("sid"));
            return sessionRepository.isActive(sessionId, userId, Instant.now(), UserStatus.ACTIVE)
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(INVALID_SESSION);
        } catch (RuntimeException exception) {
            return OAuth2TokenValidatorResult.failure(INVALID_SESSION);
        }
    }
}
