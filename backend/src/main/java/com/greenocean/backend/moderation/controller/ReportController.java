package com.greenocean.backend.moderation.controller;

import com.greenocean.backend.moderation.dto.*;
import com.greenocean.backend.moderation.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ReportController {
    private final ReportService service;
    public ReportController(ReportService service) { this.service = service; }

    @PostMapping("/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateReportRequest request) {
        return service.create(userId(jwt), request);
    }
    @GetMapping("/reports/me")
    public List<ReportResponse> mine(@AuthenticationPrincipal Jwt jwt) { return service.mine(userId(jwt)); }
    @PostMapping("/moderation-appeals")
    @ResponseStatus(HttpStatus.CREATED)
    public AppealResponse appeal(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateAppealRequest request) {
        return service.appeal(userId(jwt), request);
    }
    @GetMapping("/moderation-appeals/me")
    public List<AppealResponse> appeals(@AuthenticationPrincipal Jwt jwt) { return service.appeals(userId(jwt)); }
    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
