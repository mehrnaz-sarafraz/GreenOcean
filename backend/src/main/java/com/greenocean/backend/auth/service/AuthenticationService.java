package com.greenocean.backend.auth.service;

import com.greenocean.backend.auth.dto.CurrentUserResponse;
import com.greenocean.backend.auth.dto.LoginRequest;
import com.greenocean.backend.auth.dto.TokenResponse;
import com.greenocean.backend.auth.dto.SessionResponse;
import com.greenocean.backend.auth.entity.User;
import com.greenocean.backend.auth.entity.UserSession;
import com.greenocean.backend.auth.entity.UserStatus;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.repository.UserSessionRepository;
import com.greenocean.backend.common.exception.UnauthorizedException;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private final org.springframework.jdbc.core.JdbcTemplate db;
    private final AuthRateLimit limits;
    private final String dummyHash;
    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final DatabaseUuidGenerator databaseUuidGenerator;

    public AuthenticationService(
            UserRepository userRepository,
            UserSessionRepository sessionRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            DatabaseUuidGenerator databaseUuidGenerator,
            org.springframework.jdbc.core.JdbcTemplate db, AuthRateLimit limits
    ) {
        this.db=db;this.limits=limits;this.dummyHash=passwordEncoder.encode("dummy login timing password");
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.databaseUuidGenerator = databaseUuidGenerator;
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElse(null);
        boolean allowed=limits.allow("login:"+email,10,java.time.Duration.ofMinutes(15));
        boolean matches=passwordEncoder.matches(request.password(),user==null?dummyHash:user.getPasswordHash());
        if(user==null || !allowed) throw new UnauthorizedException(INVALID_CREDENTIALS);

        if ((user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.PENDING) || !matches) {
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }

        if(passwordEncoder.upgradeEncoding(user.getPasswordHash())) user.changePassword(passwordEncoder.encode(request.password()));
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
        return tokensFor(user, refreshToken, session.getId());
    }

    @Transactional(noRollbackFor=UnauthorizedException.class)
    public TokenResponse refresh(String refreshToken) {
        String hash=tokenService.hashRefreshToken(refreshToken);
        UserSession session = sessionRepository.findByRefreshTokenHash(hash).orElse(null);
        if(session==null) {
            db.update("UPDATE user_sessions SET revoked_at=CURRENT_TIMESTAMP WHERE id IN (SELECT session_id FROM retired_refresh_tokens WHERE token_hash=?)",hash);
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }
        Instant now = Instant.now();
        User user = session.getUser();
        if (!session.isUsableAt(now) || (user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.PENDING)) {
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }

        db.update("INSERT INTO retired_refresh_tokens(token_hash,session_id,expires_at) VALUES (?,?,?) ON CONFLICT DO NOTHING",hash,session.getId(),java.sql.Timestamp.from(session.getExpiresAt()));
        String rotatedRefreshToken = tokenService.createRefreshToken();
        session.rotate(
                tokenService.hashRefreshToken(rotatedRefreshToken),
                now.plus(tokenService.refreshTokenTtl())
        );
        return tokensFor(user, rotatedRefreshToken, session.getId());
    }

    @Transactional
    public void logout(String refreshToken) {
        sessionRepository.findByRefreshTokenHash(tokenService.hashRefreshToken(refreshToken))
                .ifPresent(UserSession::revoke);
    }

    @Transactional
    public void logoutAll(UUID userId) {
        sessionRepository.revokeAllActiveByUserId(userId, Instant.now());
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> sessions(UUID userId, UUID currentSessionId) {
        return sessionRepository.findActiveByUserId(userId, Instant.now()).stream()
                .map(session -> new SessionResponse(
                        session.getId(), session.getDeviceName(), session.getCreatedAt(), session.getLastUsedAt(),
                        session.getExpiresAt(), session.getId().equals(currentSessionId)))
                .toList();
    }

    @Transactional
    public void revokeSession(UUID userId, UUID sessionId) {
        UserSession session = sessionRepository.findOwned(sessionId, userId)
                .orElseThrow(() -> new NotFoundException("Session was not found"));
        session.revoke();
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        PasswordPolicy.validate(newPassword);
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
    }

    public CurrentUserResponse currentUser(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return new CurrentUserResponse(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString("email"),
                roles == null ? List.of() : roles
        );
    }

    private TokenResponse tokensFor(User user, String refreshToken, UUID sessionId) {
        return new TokenResponse(
                tokenService.createAccessToken(user, sessionId),
                refreshToken,
                "Bearer",
                tokenService.accessTokenTtl().toSeconds()
        );
    }

    private String normalizeDeviceName(String deviceName) {
        if (deviceName == null || deviceName.isBlank()) {
            return null;
        }
        return deviceName.trim();
    }
}
