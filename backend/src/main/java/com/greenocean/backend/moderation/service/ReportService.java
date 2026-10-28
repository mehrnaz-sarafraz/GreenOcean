package com.greenocean.backend.moderation.service;

import com.greenocean.backend.common.exception.ConflictException;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import com.greenocean.backend.moderation.dto.*;
import com.greenocean.backend.moderation.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReportService {
    private final ReportRepository repository;
    private final DatabaseUuidGenerator uuidGenerator;
    public ReportService(ReportRepository repository, DatabaseUuidGenerator uuidGenerator) {
        this.repository = repository; this.uuidGenerator = uuidGenerator;
    }

    @Transactional
    public ReportResponse create(UUID userId, CreateReportRequest request) {
        repository.limitSubmissions(userId);
        if (!repository.targetExists(request.targetType(), request.targetId(), userId))
            throw new NotFoundException("Report target was not found or is not available");
        repository.lockOpenReport(userId, request.targetType(), request.targetId());
        if (repository.duplicateOpenReport(userId, request.targetType(), request.targetId()))
            throw new ConflictException("You already have an open report for this item");
        String severity = switch (request.reason()) {
            case "SELF_HARM_CONTENT" -> "CRITICAL";
            case "HATE_SPEECH", "SEXUAL_CONTENT", "HARASSMENT" -> "HIGH";
            case "MISINFORMATION" -> "MEDIUM";
            default -> "LOW";
        };
        String[] signals = "SELF_HARM_CONTENT".equals(request.reason())
                ? new String[]{"Safety triage required"} : new String[0];
        UUID id = uuidGenerator.nextUuid();
        repository.create(id, userId, request, severity, signals);
        return repository.mine(userId).stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> mine(UUID userId) { return repository.mine(userId); }

    @Transactional
    public AppealResponse appeal(UUID userId, CreateAppealRequest request) {
        repository.limitSubmissions(userId);
        if (request.reportId() != null && !repository.reportBelongsTo(request.reportId(), userId))
            throw new NotFoundException("Report was not found");
        UUID id = uuidGenerator.nextUuid(); repository.createAppeal(id, userId, request);
        return repository.appeals(userId).stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<AppealResponse> appeals(UUID userId) { return repository.appeals(userId); }
}
