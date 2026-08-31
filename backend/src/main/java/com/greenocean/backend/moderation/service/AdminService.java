package com.greenocean.backend.moderation.service;

import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.moderation.dto.AdminDashboardResponse;
import com.greenocean.backend.moderation.dto.ModerationActionRequest;
import com.greenocean.backend.moderation.repository.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AdminService {
    private final AdminRepository repository;
    public AdminService(AdminRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public AdminDashboardResponse dashboard() { return repository.dashboard(); }

    @Transactional
    public void report(UUID reportId, UUID actorId, ModerationActionRequest request) {
        if (!repository.updateReport(reportId, actorId, request.action(), request.note())) {
            throw new NotFoundException("Report was not found");
        }
    }

    @Transactional
    public void verification(UUID verificationId, UUID actorId, ModerationActionRequest request) {
        if (!repository.updateVerification(verificationId, actorId, request.action(), request.note())) {
            throw new NotFoundException("Verification was not found");
        }
    }
}
