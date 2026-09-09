package com.greenocean.backend.auth.service;

import com.greenocean.backend.auth.dto.CurrentUserResponse;
import com.greenocean.backend.auth.dto.LoginRequest;
import com.greenocean.backend.auth.dto.TokenResponse;
import com.greenocean.backend.auth.entity.User;
import com.greenocean.backend.auth.entity.UserSession;
import com.greenocean.backend.auth.entity.UserStatus;
import com.greenocean.backend.auth.entity.UsedRefreshToken;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.repository.UserSessionRepository;
import com.greenocean.backend.auth.repository.UsedRefreshTokenRepository;
import com.greenocean.backend.common.exception.UnauthorizedException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthenticationService {
    private static final String INVALID_CREDENTIALS = "Invalid email or password";
    private static final String INVALID_REFRESH_TOKEN = "Refresh token is invalid or expired";

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final DatabaseUuidGenerator databaseUuidGenerator;
    private final UsedRefreshTokenRepository usedRefreshTokenRepository;
    private final SecurityAuditService auditService;
    private final boolean requireVerifiedEmail;
    private final String dummyPasswordHash;

    public AuthenticationService(
            UserRepository userRepository,
            UserSessionRepository sessionRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            DatabaseUuidGenerator databaseUuidGenerator,
            UsedRefreshTokenRepository usedRefreshTokenRepository,
            SecurityAuditService auditService,
            @Value("${greenocean.auth.require-verified-email:false}") boolean requireVerifiedEmail
    ) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.databaseUuidGenerator = databaseUuidGenerator;
        this.usedRefreshTokenRepository = usedRefreshTokenRepository;
        this.auditService = auditService;
        this.requireVerifiedEmail = requireVerifiedEmail;
        this.dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            // Keep unknown-account failures on the same expensive password path.
            passwordEncoder.matches(request.password(), dummyPasswordHash);
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }

        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPasswordHash());
        if (user.getStatus() != UserStatus.ACTIVE
                || (requireVerifiedEmail && !user.isEmailVerified())
                || !passwordMatches) {
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }

        user.recordLogin();
        String refreshToken = tokenService.createRefreshToken();
        UserSession session = new UserSession(
                databaseUuidGenerator.nextUuid(),
                user,
                tokenService.hashRefreshToken(refreshToken),
                normalizeDeviceName(request.deviceName()),
                Instant.now().plus(tokenService.refreshTokenTtl())
        );
        sessionRepository.save(session);
        auditService.recordForUser(user.getId(), "LOGIN_SUCCEEDED");
        return tokensFor(user, refreshToken);
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public TokenResponse refresh(String refreshToken) {
        String tokenHash = tokenService.hashRefreshToken(refreshToken);
        usedRefreshTokenRepository.deleteExpired(Instant.now());
        UserSession session = sessionRepository.findByRefreshTokenHash(tokenHash)
                .orElseGet(() -> revokeReplayedSession(tokenHash));
        Instant now = Instant.now();
        User user = session.getUser();
        if (!session.isUsableAt(now) || user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }

        usedRefreshTokenRepository.save(new UsedRefreshToken(
                tokenHash, session, now, session.getExpiresAt()));
        String rotatedRefreshToken = tokenService.createRefreshToken();
        session.rotate(
                tokenService.hashRefreshToken(rotatedRefreshToken),
                now.plus(tokenService.refreshTokenTtl())
        );
        return tokensFor(user, rotatedRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        String tokenHash = tokenService.hashRefreshToken(refreshToken);
        sessionRepository.findByRefreshTokenHash(tokenHash)
                .or(() -> usedRefreshTokenRepository.findByTokenHash(tokenHash).map(UsedRefreshToken::getSession))
                .ifPresent(session -> {
                    session.revoke();
                    auditService.recordForUser(session.getUser().getId(), "LOGOUT");
                });
    }

    @Transactional
    public void logoutAll(UUID userId) {
        sessionRepository.revokeAllActiveByUserId(userId, Instant.now());
        auditService.recordForUser(userId, "LOGOUT_ALL");
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User account is not available"));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from the current password");
        }

        user.changePassword(passwordEncoder.encode(newPassword));
        sessionRepository.revokeAllActiveByUserId(userId, Instant.now());
        auditService.recordForUser(userId, "PASSWORD_CHANGED");
    }

    public CurrentUserResponse currentUser(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return new CurrentUserResponse(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString("email"),
                roles == null ? List.of() : roles
        );
    }

    private TokenResponse tokensFor(User user, String refreshToken) {
        return new TokenResponse(
                tokenService.createAccessToken(user),
                refreshToken,
                "Bearer",
                tokenService.accessTokenTtl().toSeconds()
        );
    }

    @Transactional
    public void recordLoginFailure(String email) {
        userRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .ifPresentOrElse(
                        user -> auditService.recordForUser(user.getId(), "LOGIN_FAILED"),
                        () -> auditService.recordAnonymous("LOGIN_FAILED_UNKNOWN_IDENTITY")
                );
    }

    private UserSession revokeReplayedSession(String tokenHash) {
        usedRefreshTokenRepository.findByTokenHash(tokenHash).ifPresent(usedToken -> {
            UserSession replayedSession = usedToken.getSession();
            replayedSession.revoke();
            auditService.recordForUser(replayedSession.getUser().getId(), "REFRESH_TOKEN_REUSE_DETECTED");
        });
        throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
    }

    private String normalizeDeviceName(String deviceName) {
        if (deviceName == null || deviceName.isBlank()) {
            return null;
        }
        return deviceName.trim();
    }
}
