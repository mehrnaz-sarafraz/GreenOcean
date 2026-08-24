package com.greenocean.backend.auth.service;

import com.greenocean.backend.auth.dto.CurrentUserResponse;
import com.greenocean.backend.auth.dto.LoginRequest;
import com.greenocean.backend.auth.dto.TokenResponse;
import com.greenocean.backend.auth.entity.User;
import com.greenocean.backend.auth.entity.UserSession;
import com.greenocean.backend.auth.entity.UserStatus;
import com.greenocean.backend.auth.repository.UserRepository;
import com.greenocean.backend.auth.repository.UserSessionRepository;
import com.greenocean.backend.common.exception.UnauthorizedException;
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
            DatabaseUuidGenerator databaseUuidGenerator
    ) {
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
                .orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS));

        if (user.getStatus() != UserStatus.ACTIVE
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
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
        return tokensFor(user, refreshToken);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        UserSession session = sessionRepository.findByRefreshTokenHash(tokenService.hashRefreshToken(refreshToken))
                .orElseThrow(() -> new UnauthorizedException(INVALID_REFRESH_TOKEN));
        Instant now = Instant.now();
        User user = session.getUser();
        if (!session.isUsableAt(now) || user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }

        String rotatedRefreshToken = tokenService.createRefreshToken();
        session.rotate(
                tokenService.hashRefreshToken(rotatedRefreshToken),
                now.plus(tokenService.refreshTokenTtl())
        );
        return tokensFor(user, rotatedRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        sessionRepository.findByRefreshTokenHash(tokenService.hashRefreshToken(refreshToken))
                .ifPresent(UserSession::revoke);
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

    private String normalizeDeviceName(String deviceName) {
        if (deviceName == null || deviceName.isBlank()) {
            return null;
        }
        return deviceName.trim();
    }
}
