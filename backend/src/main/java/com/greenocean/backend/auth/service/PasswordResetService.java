package com.greenocean.backend.auth.service;

import com.greenocean.backend.auth.entity.PasswordResetToken;
import com.greenocean.backend.auth.entity.User;
import com.greenocean.backend.auth.entity.UserStatus;
import com.greenocean.backend.auth.repository.PasswordResetTokenRepository;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.repository.UserSessionRepository;
import com.greenocean.backend.common.exception.UnauthorizedException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Service
public class PasswordResetService {
    private static final String INVALID_TOKEN = "Password reset token is invalid or expired";

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final DatabaseUuidGenerator uuidGenerator;
    private final AuthTokenDelivery tokenDelivery;
    private final SecurityAuditService auditService;
    private final Duration tokenTtl;

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            UserSessionRepository sessionRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            DatabaseUuidGenerator uuidGenerator,
            AuthTokenDelivery tokenDelivery,
            SecurityAuditService auditService,
            @Value("${greenocean.auth.password-reset-token-ttl:PT30M}") Duration tokenTtl
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.uuidGenerator = uuidGenerator;
        this.tokenDelivery = tokenDelivery;
        this.auditService = auditService;
        this.tokenTtl = tokenTtl;
    }

    @Transactional
    public void requestReset(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        userRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .ifPresent(this::issueToken);
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository.findByTokenHash(tokenService.hashOpaqueToken(rawToken))
                .orElseThrow(() -> new UnauthorizedException(INVALID_TOKEN));
        Instant now = Instant.now();
        if (!token.isUsableAt(now)) {
            throw new UnauthorizedException(INVALID_TOKEN);
        }

        User user = token.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException(INVALID_TOKEN);
        }
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from the current password");
        }

        user.changePassword(passwordEncoder.encode(newPassword));
        token.consume(now);
        tokenRepository.consumeActiveForUser(user.getId(), now);
        sessionRepository.revokeAllActiveByUserId(user.getId(), now);
        auditService.recordForUser(user.getId(), "PASSWORD_RESET_COMPLETED");
    }

    private void issueToken(User user) {
        Instant now = Instant.now();
        tokenRepository.consumeActiveForUser(user.getId(), now);
        String rawToken = tokenService.createOpaqueToken();
        Instant expiresAt = now.plus(tokenTtl);
        tokenRepository.save(new PasswordResetToken(
                uuidGenerator.nextUuid(), user, tokenService.hashOpaqueToken(rawToken), expiresAt));
        tokenDelivery.sendPasswordReset(user.getEmail(), rawToken, expiresAt);
        auditService.recordForUser(user.getId(), "PASSWORD_RESET_REQUESTED");
    }
}
