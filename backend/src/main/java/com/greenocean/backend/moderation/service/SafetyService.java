package com.greenocean.backend.moderation.service;

import com.greenocean.backend.common.exception.ConflictException;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import com.greenocean.backend.moderation.dto.CreateReportRequest;
import com.greenocean.backend.moderation.dto.ReportResponse;
import com.greenocean.backend.moderation.repository.SafetyRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SafetyService {
    private final SafetyRepository repository;
    private final DatabaseUuidGenerator ids;
    public SafetyService(SafetyRepository repository, DatabaseUuidGenerator ids) { this.repository = repository; this.ids = ids; }

    @Transactional
    public ReportResponse report(UUID userId, CreateReportRequest request) {
        if (!repository.targetExistsAndVisibleTo(userId, request)) throw new NotFoundException("Report target was not found");
        try {
            repository.create(ids.nextUuid(), userId, request, severity(request.reason()));
        } catch (DuplicateKeyException exception) {
            throw new ConflictException("You already have an open report for this item");
        }
        return repository.own(userId).getFirst();
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> own(UUID userId) { return repository.own(userId); }

    @Transactional
    public void block(UUID userId, UUID targetId) {
        if (userId.equals(targetId)) throw new IllegalArgumentException("You cannot block yourself");
        repository.block(userId, targetId);
    }

    @Transactional
    public void unblock(UUID userId, UUID targetId) { repository.unblock(userId, targetId); }

    private String severity(String reason) {
        return switch (reason) { case "SELF_HARM_CONTENT" -> "CRITICAL"; case "HATE_SPEECH", "SEXUAL_CONTENT" -> "HIGH"; case "SPAM" -> "LOW"; default -> "MEDIUM"; };
    }
}
