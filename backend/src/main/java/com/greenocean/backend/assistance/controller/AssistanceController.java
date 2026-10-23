package com.greenocean.backend.assistance.controller;

import com.greenocean.backend.assistance.dto.*;
import com.greenocean.backend.assistance.service.AssistanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/account-assistance")
public class AssistanceController {
    private final AssistanceService service;
    public AssistanceController(AssistanceService service) { this.service = service; }

    @PostMapping("/password-reset")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AssistanceResponse requestPasswordReset(@Valid @RequestBody EmailRequest request) { return service.requestPasswordReset(request); }

    @PostMapping("/password-reset/confirm")
    public AssistanceResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) { return service.resetPassword(request); }

    @PostMapping("/email-verification")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AssistanceResponse requestEmailVerification(@Valid @RequestBody EmailRequest request) { return service.requestEmailVerification(request); }

    @PostMapping("/email-verification/confirm")
    public AssistanceResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) { return service.verifyEmail(request); }

    @PostMapping("/support")
    @ResponseStatus(HttpStatus.CREATED)
    public AssistanceResponse support(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SupportRequest request) {
        return service.support(jwt == null ? null : UUID.fromString(jwt.getSubject()), request);
    }
}
