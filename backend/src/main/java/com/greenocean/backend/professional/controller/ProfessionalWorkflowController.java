package com.greenocean.backend.professional.controller;

import com.greenocean.backend.professional.dto.*;
import com.greenocean.backend.professional.service.ProfessionalWorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ProfessionalWorkflowController {
    private final ProfessionalWorkflowService service;
    public ProfessionalWorkflowController(ProfessionalWorkflowService service) { this.service = service; }

    @GetMapping("/professionals/{professionalId}/reviews")
    public List<ProfessionalReviewResponse> reviews(@AuthenticationPrincipal Jwt jwt,
                                                    @PathVariable UUID professionalId) {
        return service.reviews(professionalId, userId(jwt));
    }

    @PutMapping("/professionals/{professionalId}/reviews/me")
    public ProfessionalReviewResponse review(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID professionalId,
                                             @Valid @RequestBody CreateProfessionalReviewRequest request) {
        return service.review(professionalId, userId(jwt), request);
    }

    @GetMapping("/consultations")
    public List<ConsultationResponse> consultations(@AuthenticationPrincipal Jwt jwt) {
        return service.consultations(userId(jwt));
    }

    @GetMapping("/professional/consultations")
    public List<ConsultationResponse> professionalConsultations(@AuthenticationPrincipal Jwt jwt) {
        return service.professionalConsultations(userId(jwt));
    }

    @GetMapping("/professionals/{professionalId}/availability")
    public List<ProfessionalAvailabilityResponse> availability(@PathVariable UUID professionalId) {
        return service.availability(professionalId, false);
    }

    @GetMapping("/professional/availability")
    public List<ProfessionalAvailabilityResponse> ownAvailability(@AuthenticationPrincipal Jwt jwt) {
        return service.availability(userId(jwt), true);
    }

    @PostMapping("/professional/availability")
    public ResponseEntity<ProfessionalAvailabilityResponse> createAvailability(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateAvailabilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAvailability(userId(jwt), request));
    }

    @DeleteMapping("/professional/availability/{slotId}")
    public ResponseEntity<Void> cancelAvailability(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID slotId) {
        service.cancelAvailability(slotId, userId(jwt));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/professional/consultations/{consultationId}")
    public ResponseEntity<Void> updateConsultation(@AuthenticationPrincipal Jwt jwt,
                                                    @PathVariable UUID consultationId,
                                                    @Valid @RequestBody UpdateConsultationStatusRequest request) {
        service.updateConsultationStatus(consultationId, userId(jwt), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/consultations/{consultationId}")
    public ResponseEntity<Void> cancelConsultation(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID consultationId) {
        service.cancelConsultation(consultationId, userId(jwt));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/professionals/{professionalId}/consultations")
    public ResponseEntity<ConsultationResponse> requestConsultation(@AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID professionalId, @Valid @RequestBody CreateConsultationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.requestConsultation(professionalId, userId(jwt), request));
    }

    @PostMapping("/professional-applications")
    public ResponseEntity<ProfessionalApplicationResponse> apply(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProfessionalApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.apply(userId(jwt), request));
    }

    @GetMapping("/professional-applications/me")
    public ProfessionalApplicationResponse application(@AuthenticationPrincipal Jwt jwt) {
        return service.application(userId(jwt));
    }

    @PostMapping(value = "/professional-applications/{applicationId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfessionalDocumentResponse> uploadDocument(@AuthenticationPrincipal Jwt jwt,
                                                                        @PathVariable UUID applicationId,
                                                                        @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.uploadDocument(applicationId, userId(jwt), file));
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
