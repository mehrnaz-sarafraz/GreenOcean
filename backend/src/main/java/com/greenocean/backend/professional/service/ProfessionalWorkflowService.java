package com.greenocean.backend.professional.service;

import com.greenocean.backend.common.exception.ConflictException;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import com.greenocean.backend.professional.dto.*;
import com.greenocean.backend.professional.repository.ProfessionalWorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.time.Duration;

@Service
public class ProfessionalWorkflowService {
    private final ProfessionalAuthority authority;
    private final ProfessionalWorkflowRepository repository;
    private final DatabaseUuidGenerator uuidGenerator;
    private final ProfessionalDocumentStorage documentStorage;
    private final com.greenocean.backend.identity.AccessPolicy access;

    public ProfessionalWorkflowService(ProfessionalWorkflowRepository repository, DatabaseUuidGenerator uuidGenerator,
                                       ProfessionalDocumentStorage documentStorage,com.greenocean.backend.identity.AccessPolicy access,ProfessionalAuthority authority) {
        this.access=access;this.authority=authority;
        this.repository = repository;
        this.uuidGenerator = uuidGenerator;
        this.documentStorage = documentStorage;
    }

    @Transactional(readOnly = true)
    public List<ProfessionalReviewResponse> reviews(UUID professionalId, UUID viewerId) {
        requireProfessional(professionalId);
        return repository.reviews(professionalId, viewerId);
    }

    @Transactional
    public ProfessionalReviewResponse review(UUID professionalId, UUID reviewerId, CreateProfessionalReviewRequest request) {
        requireProfessional(professionalId);
        if (professionalId.equals(reviewerId)) throw new ConflictException("You cannot review your own professional profile");
        repository.saveReview(uuidGenerator.nextUuid(), professionalId, reviewerId, request);
        return repository.reviews(professionalId, reviewerId).stream().filter(ProfessionalReviewResponse::mine)
                .findFirst().orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> consultations(UUID userId) { return repository.consultations(userId); }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> professionalConsultations(UUID userId) {
        requireProfessional(userId);
        return repository.professionalConsultations(userId);
    }

    @Transactional(readOnly = true)
    public List<ProfessionalAvailabilityResponse> availability(UUID professionalId, boolean includeUnavailable) {
        requireProfessional(professionalId);
        return repository.availability(professionalId, includeUnavailable);
    }

    @Transactional
    public ProfessionalAvailabilityResponse createAvailability(UUID professionalId, CreateAvailabilityRequest request) {
        requireProfessional(professionalId);authority.offer(professionalId,"IR",request.consultationMode());
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new IllegalArgumentException("Availability end must follow its start");
        }
        Duration duration = Duration.between(request.startsAt(), request.endsAt());
        if (duration.toMinutes() < 15 || duration.compareTo(Duration.ofHours(8)) > 0) {
            throw new IllegalArgumentException("Availability must be between 15 minutes and 8 hours");
        }
        repository.lockAvailabilitySchedule(professionalId);
        if (repository.hasAvailabilityOverlap(professionalId, request.startsAt(), request.endsAt())) {
            throw new ConflictException("Availability overlaps an existing slot");
        }
        return repository.createAvailability(uuidGenerator.nextUuid(), professionalId, request);
    }

    @Transactional
    public void cancelAvailability(UUID slotId, UUID professionalId) {
        requireProfessional(professionalId);
        if (!repository.cancelAvailability(slotId, professionalId)) {
            throw new ConflictException("Only a future available slot can be cancelled");
        }
    }

    @Transactional
    public void updateConsultationStatus(UUID consultationId, UUID userId, UpdateConsultationStatusRequest request) {
        requireProfessional(userId);
        String status = request.status().trim().toUpperCase();
        if (!List.of("CONFIRMED", "DECLINED", "COMPLETED", "CANCELLED").contains(status)) {
            throw new IllegalArgumentException("Unsupported consultation status");
        }
        if (!repository.updateConsultationStatus(consultationId, userId, status)) {
            throw new NotFoundException("Active consultation request was not found");
        }
        if (List.of("DECLINED", "CANCELLED").contains(status)) repository.releaseAvailability(consultationId);
    }

    @Transactional
    public void cancelConsultation(UUID consultationId, UUID userId) {
        if (!repository.cancelOwnConsultation(consultationId, userId)) {
            throw new NotFoundException("Active consultation request was not found");
        }
        repository.releaseAvailability(consultationId);
    }

    @Transactional
    public ConsultationResponse requestConsultation(UUID professionalId, UUID userId, CreateConsultationRequest request) {
        authority.lock(professionalId);
        access.require(userId,"PROFESSIONAL_CARE");access.require(professionalId,"PROFESSIONAL_CARE");
        requireProfessional(professionalId);
        if (professionalId.equals(userId)) throw new ConflictException("You cannot request a consultation with yourself");
        if (request.availabilitySlotId() == null) throw new IllegalArgumentException("Select a published availability slot");
        ProfessionalAvailabilityResponse slot = repository.availableSlotForUpdate(request.availabilitySlotId(), professionalId)
                .orElseThrow(() -> new ConflictException("This availability slot is no longer available"));
        access.requireCare(userId,professionalId,slot.consultationMode());
        UUID id = uuidGenerator.nextUuid();
        repository.bookAvailability(slot.id());
        repository.createConsultation(id, userId, professionalId, slot, request);
        return repository.consultations(userId).stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow();
    }

    @Transactional
    public ProfessionalApplicationResponse apply(UUID userId, ProfessionalApplicationRequest request) {
        if(!"IR".equals(request.country()))throw new IllegalArgumentException("Iran(IR) is the initial professional verification jurisdiction; other jurisdictions need separate approval");
        if (repository.hasPendingApplication(userId)) throw new ConflictException("A professional application is already under review");
        UUID application=uuidGenerator.nextUuid();
        repository.submitApplication(application, userId, request);
        authority.application(application,userId,request.professionalType().toUpperCase(java.util.Locale.ROOT),request.licenseNumber().trim());
        return repository.latestApplication(userId).orElseThrow();
    }

    @Transactional(readOnly = true)
    public ProfessionalApplicationResponse application(UUID userId) {
        return repository.latestApplication(userId).orElseThrow(() -> new NotFoundException("Professional application was not found"));
    }

    @Transactional
    public ProfessionalDocumentResponse uploadDocument(UUID applicationId, UUID userId,
                                                        org.springframework.web.multipart.MultipartFile file) {
        authority.actor(userId);
        if (!repository.applicationOwnedBy(applicationId, userId)) {
            throw new NotFoundException("Open professional application was not found");
        }
        ProfessionalDocumentStorage.StoredDocument stored = documentStorage.store(applicationId, file);
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization(){@Override public void afterCompletion(int status){if(status!=STATUS_COMMITTED)documentStorage.delete(stored.storageKey());}});
        var response=repository.addDocument(uuidGenerator.nextUuid(), applicationId, stored);
        authority.audit(userId,"CREDENTIAL_DOCUMENT_UPLOADED",applicationId,"Encrypted credential quarantine upload");
        return response;
    }

    private void requireProfessional(UUID id) {
        if (!repository.professionalExists(id)) throw new NotFoundException("Professional was not found");
    }
}
