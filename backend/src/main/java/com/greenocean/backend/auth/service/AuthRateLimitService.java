package com.greenocean.backend.auth.service;

import com.greenocean.backend.common.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class AuthRateLimitService {

    private final Map<String, WindowCounter> loginIpCounters = new HashMap<>();
    private final Map<String, LoginFailureState> loginFailureCounters = new HashMap<>();
    private final Map<String, WindowCounter> registerIpCounters = new HashMap<>();
    private final Map<String, WindowCounter> refreshIpCounters = new HashMap<>();

    private final int loginIpMax;
    private final Duration loginIpWindow;

    private final int loginFailureMax;
    private final Duration loginLockDuration;

    private final int registerIpMax;
    private final Duration registerIpWindow;

    private final int refreshIpMax;
    private final Duration refreshIpWindow;

    private long operations;

    public AuthRateLimitService(
            @Value("${greenocean.security.rate-limit.login-ip-max:20}") int loginIpMax,
            @Value("${greenocean.security.rate-limit.login-ip-window:PT1M}") Duration loginIpWindow,
            @Value("${greenocean.security.rate-limit.login-failure-max:5}") int loginFailureMax,
            @Value("${greenocean.security.rate-limit.login-lock-duration:PT15M}") Duration loginLockDuration,
            @Value("${greenocean.security.rate-limit.register-ip-max:5}") int registerIpMax,
            @Value("${greenocean.security.rate-limit.register-ip-window:PT10M}") Duration registerIpWindow,
            @Value("${greenocean.security.rate-limit.refresh-ip-max:60}") int refreshIpMax,
            @Value("${greenocean.security.rate-limit.refresh-ip-window:PT1M}") Duration refreshIpWindow
    ) {
        this.loginIpMax = loginIpMax;
        this.loginIpWindow = loginIpWindow;
        this.loginFailureMax = loginFailureMax;
        this.loginLockDuration = loginLockDuration;
        this.registerIpMax = registerIpMax;
        this.registerIpWindow = registerIpWindow;
        this.refreshIpMax = refreshIpMax;
        this.refreshIpWindow = refreshIpWindow;
    }

    public synchronized void checkLoginAllowed(String email, String clientIp) {
        Instant now = Instant.now();

        checkWindow(
                loginIpCounters,
                normalizeIp(clientIp),
                loginIpMax,
                loginIpWindow,
                now,
                "Too many login attempts. Please try again later."
        );

        String identity = normalizeEmail(email);
        LoginFailureState failureState = loginFailureCounters.get(identity);

        if (failureState != null && failureState.lockedUntil() != null) {
            if (failureState.lockedUntil().isAfter(now)) {
                throw new TooManyRequestsException(
                        "Too many failed login attempts. Please try again later.",
                        secondsUntil(failureState.lockedUntil(), now)
                );
            }

            loginFailureCounters.remove(identity);
        }

        cleanupIfNeeded(now);
    }

    public synchronized void recordLoginFailure(String email) {
        Instant now = Instant.now();
        String identity = normalizeEmail(email);

        LoginFailureState current = loginFailureCounters.get(identity);

        if (current != null
                && current.lockedUntil() != null
                && current.lockedUntil().isAfter(now)) {
            return;
        }

        int failures = current == null ? 1 : current.failures() + 1;

        if (failures >= loginFailureMax) {
            loginFailureCounters.put(
                    identity,
                    new LoginFailureState(
                            failures,
                            now.plus(loginLockDuration)
                    )
            );
        } else {
            loginFailureCounters.put(
                    identity,
                    new LoginFailureState(failures, null)
            );
        }

        cleanupIfNeeded(now);
    }

    public synchronized void recordLoginSuccess(String email) {
        loginFailureCounters.remove(normalizeEmail(email));
    }

    public synchronized void checkRegistrationAllowed(String clientIp) {
        Instant now = Instant.now();

        checkWindow(
                registerIpCounters,
                normalizeIp(clientIp),
                registerIpMax,
                registerIpWindow,
                now,
                "Too many registration attempts. Please try again later."
        );

        cleanupIfNeeded(now);
    }

    public synchronized void checkRefreshAllowed(String clientIp) {
        Instant now = Instant.now();

        checkWindow(
                refreshIpCounters,
                normalizeIp(clientIp),
                refreshIpMax,
                refreshIpWindow,
                now,
                "Too many token refresh attempts. Please try again later."
        );

        cleanupIfNeeded(now);
    }

    private void checkWindow(
            Map<String, WindowCounter> counters,
            String key,
            int maxRequests,
            Duration window,
            Instant now,
            String errorMessage
    ) {
        WindowCounter current = counters.get(key);

        if (current == null || !current.windowEndsAt().isAfter(now)) {
            counters.put(
                    key,
                    new WindowCounter(
                            1,
                            now.plus(window)
                    )
            );
            return;
        }

        if (current.count() >= maxRequests) {
            throw new TooManyRequestsException(
                    errorMessage,
                    secondsUntil(current.windowEndsAt(), now)
            );
        }

        counters.put(
                key,
                new WindowCounter(
                        current.count() + 1,
                        current.windowEndsAt()
                )
        );
    }

    private void cleanupIfNeeded(Instant now) {
        operations++;

        if (operations % 1000 != 0) {
            return;
        }

        loginIpCounters.entrySet().removeIf(
                entry -> !entry.getValue().windowEndsAt().isAfter(now)
        );

        registerIpCounters.entrySet().removeIf(
                entry -> !entry.getValue().windowEndsAt().isAfter(now)
        );

        refreshIpCounters.entrySet().removeIf(
                entry -> !entry.getValue().windowEndsAt().isAfter(now)
        );

        loginFailureCounters.entrySet().removeIf(entry -> {
            LoginFailureState state = entry.getValue();

            return state.lockedUntil() != null
                    && !state.lockedUntil().isAfter(now);
        });
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return "unknown";
        }

        return clientIp.trim();
    }

    private long secondsUntil(Instant target, Instant now) {
        return Math.max(
                1,
                Duration.between(now, target).toSeconds()
        );
    }

    private record WindowCounter(
            int count,
            Instant windowEndsAt
    ) {
    }

    private record LoginFailureState(
            int failures,
            Instant lockedUntil
    ) {
    }
}