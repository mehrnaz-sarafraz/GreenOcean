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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class EmailVerificationService {

    private static final String INVALID_TOKEN =
            "Email verification token is invalid or expired";

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final DatabaseUuidGenerator databaseUuidGenerator;

    private final Duration tokenTtl;

    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(
            EmailVerificationTokenRepository tokenRepository,
            UserRepository userRepository,
            DatabaseUuidGenerator databaseUuidGenerator,
            @Value("${greenocean.auth.email-verification-token-ttl:PT24H}")
            Duration tokenTtl
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.databaseUuidGenerator = databaseUuidGenerator;
        this.tokenTtl = tokenTtl;
    }

    @Transactional
    public String createToken(User user) {

        String rawToken = generateSecureToken();

        EmailVerificationToken token =
                new EmailVerificationToken(
                        databaseUuidGenerator.nextUuid(),
                        user,
                        hashToken(rawToken),
                        Instant.now().plus(tokenTtl)
                );

        tokenRepository.save(token);

        return rawToken;
    }

    @Transactional
    public void verify(String rawToken) {

        String tokenHash = hashToken(rawToken);

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
        token.consume();

        userRepository.save(user);
    }

    private String generateSecureToken() {

        byte[] bytes = new byte[32];

        secureRandom.nextBytes(bytes);

        return HexFormat.of().formatHex(bytes);
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
                    );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    exception
            );
        }
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