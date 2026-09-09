package com.greenocean.backend.auth.service;

import com.greenocean.backend.auth.entity.EmailVerificationToken;
import com.greenocean.backend.auth.entity.User;
import com.greenocean.backend.auth.repository.EmailVerificationTokenRepository;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.common.exception.UnauthorizedException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Service
public class EmailVerificationService {

    private static final String INVALID_TOKEN =
            "Email verification token is invalid or expired";

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final DatabaseUuidGenerator databaseUuidGenerator;
    private final TokenService tokenService;
    private final AuthTokenDelivery tokenDelivery;
    private final SecurityAuditService auditService;

    private final Duration tokenTtl;

    public EmailVerificationService(
            EmailVerificationTokenRepository tokenRepository,
            UserRepository userRepository,
            DatabaseUuidGenerator databaseUuidGenerator,
            TokenService tokenService,
            AuthTokenDelivery tokenDelivery,
            SecurityAuditService auditService,
            @Value("${greenocean.auth.email-verification-token-ttl:PT24H}")
            Duration tokenTtl
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.databaseUuidGenerator = databaseUuidGenerator;
        this.tokenService = tokenService;
        this.tokenDelivery = tokenDelivery;
        this.auditService = auditService;
        this.tokenTtl = tokenTtl;
    }

    @Transactional
    public String createToken(User user) {

        Instant now = Instant.now();
        tokenRepository.consumeActiveForUser(user.getId(), now);
        String rawToken = tokenService.createOpaqueToken();
        Instant expiresAt = now.plus(tokenTtl);

        EmailVerificationToken token =
                new EmailVerificationToken(
                        databaseUuidGenerator.nextUuid(),
                        user,
                        tokenService.hashOpaqueToken(rawToken),
                        expiresAt
                );

        tokenRepository.save(token);
        tokenDelivery.sendEmailVerification(user.getEmail(), rawToken, expiresAt);

        return rawToken;
    }

    @Transactional
    public void verify(String rawToken) {

        String tokenHash = tokenService.hashOpaqueToken(rawToken);

        EmailVerificationToken token =
                tokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(
                                () -> new UnauthorizedException(INVALID_TOKEN)
                        );

        Instant now = Instant.now();

        if (!token.isUsableAt(now)) {
            throw new UnauthorizedException(INVALID_TOKEN);
        }

        User user = token.getUser();

        user.verifyEmail();
        token.consume(now);
        tokenRepository.consumeActiveForUser(user.getId(), now);

        userRepository.save(user);
        auditService.recordForUser(user.getId(), "EMAIL_VERIFIED");
    }

    @Transactional
    public void resend(String email) {

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .ifPresent(user -> {

                    if (!user.isEmailVerified()) {
                        createToken(user);
                    }

                });
    }
}
