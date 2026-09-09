package com.greenocean.backend.professional.controller;

import com.greenocean.backend.professional.dto.SubmitVerificationRequest;
import com.greenocean.backend.professional.dto.VerificationStatusResponse;
import com.greenocean.backend.professional.service.ProfessionalVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/professional-verifications")
public class ProfessionalVerificationController {
    private final ProfessionalVerificationService service;
    public ProfessionalVerificationController(ProfessionalVerificationService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<VerificationStatusResponse> submit(@AuthenticationPrincipal Jwt jwt,
                                                              @Valid @RequestBody SubmitVerificationRequest request) {
        return ResponseEntity.status(201).body(service.submit(userId(jwt), request));
    }

    @GetMapping("/mine")
    public VerificationStatusResponse status(@AuthenticationPrincipal Jwt jwt) { return service.status(userId(jwt)); }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
