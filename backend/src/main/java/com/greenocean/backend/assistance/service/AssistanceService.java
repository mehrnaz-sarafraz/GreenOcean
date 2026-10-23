package com.greenocean.backend.assistance.service;

import com.greenocean.backend.assistance.dto.*;
import com.greenocean.backend.assistance.repository.AssistanceRepository;
import com.greenocean.backend.auth.entity.User;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.repository.UserSessionRepository;
import com.greenocean.backend.auth.service.TokenService;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class AssistanceService {
    private static final String GENERIC_RESET_MESSAGE = "If the account exists, recovery instructions have been queued.";
    private final AssistanceRepository repository;
    private final UserRepository users;
    private final UserSessionRepository sessions;
    private final TokenService tokens;
    private final PasswordEncoder passwordEncoder;
    private final DatabaseUuidGenerator uuidGenerator;
    private final SecureRandom random = new SecureRandom();

    public AssistanceService(AssistanceRepository repository, UserRepository users, UserSessionRepository sessions,
                             TokenService tokens, PasswordEncoder passwordEncoder, DatabaseUuidGenerator uuidGenerator) {
        this.repository = repository; this.users = users; this.sessions = sessions; this.tokens = tokens;
        this.passwordEncoder = passwordEncoder; this.uuidGenerator = uuidGenerator;
    }

    @Transactional
    public AssistanceResponse requestPasswordReset(EmailRequest request) {
        UUID requestId = uuidGenerator.nextUuid();
        users.findByEmailIgnoreCase(normalize(request.email())).ifPresent(user -> {
            repository.invalidatePasswordResets(user.getId());
            String token = tokens.createRefreshToken();
            repository.createPasswordReset(requestId, user.getId(), tokens.hashRefreshToken(token), OffsetDateTime.now().plusMinutes(30));
            repository.enqueuePasswordReset(uuidGenerator.nextUuid(), user.getEmail(), token);
        });
        return new AssistanceResponse(requestId, GENERIC_RESET_MESSAGE);
    }

    @Transactional
    public AssistanceResponse resetPassword(ResetPasswordRequest request) {
        UUID userId = repository.consumePasswordReset(tokens.hashRefreshToken(request.token()))
                .orElseThrow(() -> new IllegalArgumentException("Recovery token is invalid or expired"));
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("User was not found"));
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        sessions.revokeAllActiveByUserId(userId, Instant.now());
        return new AssistanceResponse(userId, "Password updated. Existing sessions were revoked.");
    }

    @Transactional
    public AssistanceResponse requestEmailVerification(EmailRequest request) {
        UUID requestId = uuidGenerator.nextUuid();
        users.findByEmailIgnoreCase(normalize(request.email())).ifPresent(user -> {
            repository.invalidateVerificationCodes(user.getId());
            String code = String.format("%06d", random.nextInt(1_000_000));
            repository.createVerificationCode(requestId, user.getId(), tokens.hashRefreshToken(code), OffsetDateTime.now().plusMinutes(15));
            repository.enqueueVerification(uuidGenerator.nextUuid(), user.getEmail(), code);
        });
        return new AssistanceResponse(requestId, "If the account exists, a verification code has been queued.");
    }

    @Transactional
    public AssistanceResponse verifyEmail(VerifyEmailRequest request) {
        User user = users.findByEmailIgnoreCase(normalize(request.email()))
                .orElseThrow(() -> new IllegalArgumentException("Verification code is invalid or expired"));
        if (!repository.consumeVerificationCode(user.getId(), tokens.hashRefreshToken(request.code())))
            throw new IllegalArgumentException("Verification code is invalid or expired");
        repository.verifyUser(user.getId());
        return new AssistanceResponse(user.getId(), "Email address verified.");
    }

    @Transactional
    public AssistanceResponse support(UUID userId, SupportRequest request) {
        UUID id = uuidGenerator.nextUuid();
        repository.createSupportRequest(id, userId, normalize(request.email()), request.message().trim());
        return new AssistanceResponse(id, "Support request received.");
    }

    private String normalize(String email) { return email.trim().toLowerCase(Locale.ROOT); }
}
