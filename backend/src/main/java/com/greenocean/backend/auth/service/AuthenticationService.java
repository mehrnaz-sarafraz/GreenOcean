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
    private final SessionPolicy policy;private final java.time.Clock clock;private final SecurityEvents events;
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
            org.springframework.jdbc.core.JdbcTemplate db, AuthRateLimit limits, SessionPolicy policy,java.time.Clock clock,SecurityEvents events
    ) {
        this.db=db;this.limits=limits;this.policy=policy;this.clock=clock;this.events=events;this.dummyHash=passwordEncoder.encode("dummy login timing password");
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.databaseUuidGenerator = databaseUuidGenerator;
    }

    @Transactional(noRollbackFor={UnauthorizedException.class,com.greenocean.backend.common.exception.TooManyRequestsException.class})
    public TokenResponse login(LoginRequest request) {return login(request,"MOBILE");}
    @Transactional(noRollbackFor={UnauthorizedException.class,com.greenocean.backend.common.exception.TooManyRequestsException.class})
    public TokenResponse login(LoginRequest request,String client) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        boolean allowed=limits.allow("login:"+email,10,java.time.Duration.ofMinutes(15));
        if(!allowed){events.record(null,null,SecurityEvents.Event.AUTH_RATE_LIMITED);throw new com.greenocean.backend.common.exception.TooManyRequestsException(INVALID_CREDENTIALS+". Wait before trying again.",900);}
        User user = userRepository.lockByEmail(email)
                .orElse(null);
        boolean matches=passwordEncoder.matches(request.password(),user==null?dummyHash:user.getPasswordHash());
        if(user==null){events.record(null,null,SecurityEvents.Event.LOGIN_FAILURE);throw new UnauthorizedException(INVALID_CREDENTIALS);}

        if ((user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.PENDING) || !matches) {
            events.record(user.getId(),null,SecurityEvents.Event.LOGIN_FAILURE);throw new UnauthorizedException(INVALID_CREDENTIALS);
        }

        if(passwordEncoder.upgradeEncoding(user.getPasswordHash())) user.changePassword(passwordEncoder.encode(request.password()));
        user.recordLogin();
        String refreshToken = tokenService.createRefreshToken();
        UserSession session = new UserSession(
                databaseUuidGenerator.nextUuid(),
                user,
                tokenService.hashRefreshToken(refreshToken),
                normalizeDeviceName(request.deviceName()),
                clock.instant().plus(tokenService.refreshTokenTtl())
        );
        String scope=user.getStatus()==UserStatus.PENDING?"PENDING":SessionPolicy.privileged(user.getRoles().stream().map(com.greenocean.backend.auth.entity.Role::getName).toList())?"PRIVILEGED":"MEMBER";
        session.initialize(client,scope,clock.instant(),policy);
        sessionRepository.saveAndFlush(session);events.record(user.getId(),session.getId(),SecurityEvents.Event.LOGIN_SUCCESS);
        return tokensFor(user, refreshToken, session.getId());
    }

    @Transactional(noRollbackFor=UnauthorizedException.class)
    public TokenResponse refresh(String refreshToken) {
        String hash=tokenService.hashRefreshToken(refreshToken);
        // One lock order for refresh, revocation, password changes and recovery: user, then session.
        var owner=db.queryForList("SELECT user_id FROM user_sessions WHERE refresh_token_hash=? UNION SELECT s.user_id FROM user_sessions s JOIN retired_refresh_tokens r ON r.session_id=s.id WHERE r.token_hash=?",UUID.class,hash,hash);
        if(!owner.isEmpty())userRepository.lockById(owner.getFirst());
        UserSession session = sessionRepository.findByRefreshTokenHash(hash).orElse(null);
        if(session==null) {
            int affected=db.update("UPDATE user_sessions SET revoked_at=CURRENT_TIMESTAMP WHERE revoked_at IS NULL AND id IN (SELECT session_id FROM retired_refresh_tokens WHERE token_hash=?)",hash);
            if(affected>0)events.record(owner.isEmpty()?null:owner.getFirst(),null,SecurityEvents.Event.REFRESH_REPLAY);
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }
        Instant now = clock.instant();
        User user = session.getUser();
        if (!session.isUsableAt(now) || (user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.PENDING)) {
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }

        db.update("INSERT INTO retired_refresh_tokens(token_hash,session_id,expires_at) VALUES (?,?,?) ON CONFLICT DO NOTHING",hash,session.getId(),java.sql.Timestamp.from(session.getExpiresAt()));
        String rotatedRefreshToken = tokenService.createRefreshToken();
        session.rotate(
                tokenService.hashRefreshToken(rotatedRefreshToken),
                SessionPolicy.earlier(now.plus(tokenService.refreshTokenTtl()),session.getOverallExpiresAt())
        );
        return tokensFor(user, rotatedRefreshToken, session.getId());
    }

    @Transactional
    public void logout(String refreshToken) {
        String hash=tokenService.hashRefreshToken(refreshToken);
        var owners=db.queryForList("SELECT user_id FROM user_sessions WHERE refresh_token_hash=? UNION SELECT s.user_id FROM user_sessions s JOIN retired_refresh_tokens r ON r.session_id=s.id WHERE r.token_hash=?",UUID.class,hash,hash);
        if(owners.isEmpty())return;userRepository.lockById(owners.getFirst());
        db.update("UPDATE user_sessions SET revoked_at=CURRENT_TIMESTAMP WHERE revoked_at IS NULL AND (refresh_token_hash=? OR id IN (SELECT session_id FROM retired_refresh_tokens WHERE token_hash=?))",hash,hash);
        events.record(owners.getFirst(),null,SecurityEvents.Event.LOGOUT);
    }
    @Transactional
    public void logoutAll(UUID userId) {
        userRepository.lockById(userId);sessionRepository.revokeAllActiveByUserId(userId,clock.instant());events.record(userId,null,SecurityEvents.Event.LOGOUT_ALL);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> sessions(UUID userId, UUID currentSessionId) {
        return sessionRepository.findActiveByUserId(userId, clock.instant()).stream()
                .map(session -> new SessionResponse(
                        session.getId(), session.getDeviceName(), session.getCreatedAt(), session.getLastActivityAt(),
                        session.getExpiresAt(), session.getId().equals(currentSessionId)))
                .toList();
    }

    @Transactional
    public void revokeSession(UUID userId, UUID sessionId) {
        userRepository.lockById(userId);
        UserSession session = sessionRepository.findOwned(sessionId, userId)
                .orElseThrow(() -> new NotFoundException("Session was not found"));
        session.revoke();events.record(userId,sessionId,SecurityEvents.Event.SESSION_REVOKED);
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        PasswordPolicy.validate(newPassword);
        User user = userRepository.lockById(userId)
                .orElseThrow(() -> new UnauthorizedException("User account is not available"));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from the current password");
        }

        user.changePassword(passwordEncoder.encode(newPassword));
        sessionRepository.revokeAllActiveByUserId(userId, clock.instant());events.record(userId,null,SecurityEvents.Event.PASSWORD_CHANGED);
    }

    public CurrentUserResponse currentUser(Jwt jwt) {
        UUID userId=UUID.fromString(jwt.getSubject());
        List<String> roles = db.queryForList("SELECT r.name FROM roles r JOIN user_roles ur ON ur.role_id=r.id WHERE ur.user_id=? ORDER BY r.name",String.class,userId);
        return new CurrentUserResponse(
                UUID.fromString(jwt.getSubject()),
                db.queryForObject("SELECT email FROM users WHERE id=?",String.class,userId),
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
        String safe=deviceName.replaceAll("[\\p{Cc}\\p{Cf}]", "").trim();
        return safe.isEmpty()?null:safe;
    }
}
