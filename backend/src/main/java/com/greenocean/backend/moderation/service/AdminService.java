package com.greenocean.backend.moderation.service;

import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.moderation.dto.AdminDashboardResponse;
import com.greenocean.backend.moderation.dto.AdminOperationsResponse;
import com.greenocean.backend.moderation.dto.ModerationActionRequest;
import com.greenocean.backend.moderation.dto.BroadcastRequest;
import com.greenocean.backend.moderation.dto.CreateCampaignRequest;
import com.greenocean.backend.moderation.dto.CreatePolicyRequest;
import com.greenocean.backend.moderation.dto.AdminStatusRequest;
import com.greenocean.backend.moderation.dto.AdminExportFile;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import com.greenocean.backend.moderation.repository.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AdminService {
    private final com.greenocean.backend.community.service.GroupSafety groupSafety;
    private final AdminRepository repository;
    private final DatabaseUuidGenerator uuidGenerator;
    public AdminService(AdminRepository repository, DatabaseUuidGenerator uuidGenerator,com.greenocean.backend.community.service.GroupSafety groupSafety) {
        this.groupSafety=groupSafety;
        this.repository = repository; this.uuidGenerator = uuidGenerator;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse dashboard() { return repository.dashboard(); }

    @Transactional(readOnly = true)
    public AdminOperationsResponse operations() { return repository.operations(); }

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

    @Transactional
    public void review(UUID id, UUID actorId, ModerationActionRequest request) {
        if (!repository.updateReview(id, actorId, request.action(), request.note()))
            throw new NotFoundException("Review was not found");
    }

    @Transactional
    public void listenerApplication(UUID id, UUID actorId, ModerationActionRequest request) {
        if (!repository.updateListenerApplication(id, actorId, request.action(), request.note()))
            throw new NotFoundException("Listener application was not found");
    }

    @Transactional
    public void campaign(UUID id, UUID actorId, ModerationActionRequest request) {
        if (!repository.updateCampaign(id, actorId, request.action()))
            throw new NotFoundException("Campaign was not found");
    }

    @Transactional
    public void appeal(UUID id, UUID actorId, ModerationActionRequest request) {
        if (!repository.updateAppeal(id, actorId, request.action(), request.note()))
            throw new NotFoundException("Appeal was not found");
    }

    @Transactional
    public UUID broadcast(UUID actorId, BroadcastRequest request) {
        return repository.createBroadcast(uuidGenerator.nextUuid(), actorId, request);
    }

    @Transactional
    public UUID createExport(UUID actorId, String type) {
        return repository.createExport(uuidGenerator.nextUuid(), actorId, type.toUpperCase());
    }

    @Transactional
    public AdminExportFile downloadExport(UUID id, UUID actorId) {
        return repository.downloadExport(id, actorId)
                .orElseThrow(() -> new NotFoundException("Completed export was not found or has expired"));
    }

    @Transactional
    public UUID createCampaign(UUID actorId, CreateCampaignRequest request) {
        return repository.createCampaign(uuidGenerator.nextUuid(), actorId, request);
    }

    @Transactional
    public UUID createPolicy(UUID actorId, CreatePolicyRequest request) {
        if(Boolean.TRUE.equals(request.publish()))throw new IllegalArgumentException("Create an unpublished draft; independent review and explicit approved publication are required");
        return repository.createPolicy(uuidGenerator.nextUuid(), actorId, request);
    }

    @Transactional
    public void userStatus(UUID id, UUID actorId, AdminStatusRequest request) {
        String status = request.status().trim().toUpperCase();
        if (!java.util.List.of("ACTIVE", "SUSPENDED").contains(status)) throw new IllegalArgumentException("Unsupported user status");
        if (!repository.updateUserStatus(id, actorId, status, request.note())) throw new NotFoundException("Active user was not found");
    }

    @Transactional
    public void communityStatus(UUID id, UUID actorId, AdminStatusRequest request) {
        String status = request.status().trim().toUpperCase();
        if (!java.util.List.of("ACTIVE", "ARCHIVED").contains(status)) throw new IllegalArgumentException("Unsupported community status");
        if(request.note()==null||request.note().isBlank())throw new IllegalArgumentException("Explain the group lifecycle decision");
        groupSafety.lifecycle(id,actorId,status,request.note(),true);
    }

    @Transactional
    public void documentStatus(UUID id, UUID actorId, AdminStatusRequest request) {
        String status = request.status().trim().toUpperCase();
        if (!java.util.List.of("CLEAN", "REJECTED").contains(status)) throw new IllegalArgumentException("Unsupported document status");
        if (!repository.updateDocumentScan(id, actorId, status)) throw new NotFoundException("Credential document was not found");
    }

    @Transactional
    public void supportRequest(UUID id, UUID actorId, AdminStatusRequest request) {
        String status = request.status().trim().toUpperCase();
        if (!java.util.List.of("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED").contains(status)) throw new IllegalArgumentException("Unsupported support status");
        if (!repository.updateSupportRequest(id, actorId, status, request.note())) throw new NotFoundException("Support request was not found");
    }
}
