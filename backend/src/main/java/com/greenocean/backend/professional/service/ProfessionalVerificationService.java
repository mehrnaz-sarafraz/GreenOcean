package com.greenocean.backend.professional.service;

import com.greenocean.backend.common.exception.ConflictException;
import com.greenocean.backend.common.exception.ForbiddenException;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import com.greenocean.backend.professional.dto.SubmitVerificationRequest;
import com.greenocean.backend.professional.dto.VerificationStatusResponse;
import com.greenocean.backend.professional.repository.ProfessionalVerificationRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProfessionalVerificationService {
    private final ProfessionalVerificationRepository repository;
    private final DatabaseUuidGenerator ids;
    public ProfessionalVerificationService(ProfessionalVerificationRepository repository, DatabaseUuidGenerator ids) {
        this.repository = repository; this.ids = ids;
    }

    @Transactional
    public VerificationStatusResponse submit(UUID userId, SubmitVerificationRequest request) {
        if (request.documentIds().stream().distinct().count() != request.documentIds().size()) throw new IllegalArgumentException("documentIds must be unique");
        if (!repository.documentsAvailable(userId, request.documentIds())) throw new ForbiddenException("One or more verification documents are unavailable");
        try { repository.submit(ids.nextUuid(), userId, request); }
        catch (DuplicateKeyException exception) { throw new ConflictException("A verification is already awaiting review"); }
        return status(userId);
    }

    @Transactional(readOnly = true)
    public VerificationStatusResponse status(UUID userId) {
        return repository.latest(userId).orElseThrow(() -> new NotFoundException("No verification submission was found"));
    }
}
