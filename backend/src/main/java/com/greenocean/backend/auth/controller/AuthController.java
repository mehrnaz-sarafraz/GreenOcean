package com.greenocean.backend.auth.controller;

import com.greenocean.backend.auth.dto.ResendEmailVerificationRequest;
import com.greenocean.backend.auth.dto.VerifyEmailRequest;
import com.greenocean.backend.auth.service.EmailVerificationService;
import com.greenocean.backend.auth.dto.ChangePasswordRequest;
import com.greenocean.backend.auth.dto.CurrentUserResponse;
import com.greenocean.backend.auth.dto.LoginRequest;
import com.greenocean.backend.auth.dto.LogoutRequest;
import com.greenocean.backend.auth.dto.RefreshTokenRequest;
import com.greenocean.backend.auth.dto.RegisterRequest;
import com.greenocean.backend.auth.dto.RegisterResponse;
import com.greenocean.backend.auth.dto.TokenResponse;
import com.greenocean.backend.auth.service.AuthRateLimitService;
import com.greenocean.backend.auth.service.AuthenticationService;
import com.greenocean.backend.auth.service.RegistrationService;
import com.greenocean.backend.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;
    private final AuthRateLimitService authRateLimitService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(
            RegistrationService registrationService,
            AuthenticationService authenticationService,
            AuthRateLimitService authRateLimitService,
            EmailVerificationService emailVerificationService
    ) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
        this.authRateLimitService = authRateLimitService;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        emailVerificationService.verify(request.token());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(
            @Valid @RequestBody ResendEmailVerificationRequest request
    ) {
        emailVerificationService.resend(request.email());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        authRateLimitService.checkRegistrationAllowed(
                clientIp(httpRequest)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registrationService.register(request));
    }

    @PostMapping("/login")
    public TokenResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = clientIp(httpRequest);

        authRateLimitService.checkLoginAllowed(
                request.email(),
                clientIp
        );

        try {
            TokenResponse response =
                    authenticationService.login(request);

            authRateLimitService.recordLoginSuccess(
                    request.email()
            );

            return response;
        } catch (UnauthorizedException exception) {
            authRateLimitService.recordLoginFailure(
                    request.email()
            );

            throw exception;
        }
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        authRateLimitService.checkRefreshAllowed(
                clientIp(httpRequest)
        );

        return authenticationService.refresh(
                request.refreshToken()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequest request
    ) {
        authenticationService.logout(
                request.refreshToken()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public CurrentUserResponse me(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return authenticationService.currentUser(jwt);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authenticationService.changePassword(
                UUID.fromString(jwt.getSubject()),
                request.currentPassword(),
                request.newPassword()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
            @AuthenticationPrincipal Jwt jwt
    ) {
        authenticationService.logoutAll(
                UUID.fromString(jwt.getSubject())
        );

        return ResponseEntity.noContent().build();
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}