package com.greenocean.backend.listener.service;

import com.greenocean.backend.common.exception.ConflictException;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import com.greenocean.backend.listener.dto.*;
import com.greenocean.backend.listener.repository.ListenerRepository;
import com.greenocean.backend.messaging.repository.MessagingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListenerService {
    private final com.greenocean.backend.listener.live.LiveSupportSettings liveSettings;
    private final com.greenocean.backend.identity.AccessPolicy access;
    private final ListenerRepository repository;
    private final MessagingRepository messagingRepository;
    private final DatabaseUuidGenerator uuidGenerator;
    public ListenerService(ListenerRepository repository, MessagingRepository messagingRepository,
                           DatabaseUuidGenerator uuidGenerator,com.greenocean.backend.identity.AccessPolicy access,com.greenocean.backend.listener.live.LiveSupportSettings liveSettings) {
        this.liveSettings=liveSettings;
        this.access=access;
        this.repository = repository; this.messagingRepository = messagingRepository; this.uuidGenerator = uuidGenerator;
    }

    @Transactional(readOnly = true)
    public List<ListenerResponse> listeners(UUID userId, boolean onlineOnly, String topic) {
        return liveSettings.enabled()?repository.listeners(userId, onlineOnly, null):List.of();
    }

    @Transactional
    public ListenerMatchResponse match(UUID userId, UUID requestedListenerId, String topic) {
        throw new com.greenocean.backend.listener.live.LiveSupportException(410,"CLIENT_UPGRADE_REQUIRED",
                "Direct listener matching is retired. Create a live request and wait for listener acceptance.");
    }

    @Transactional
    public ListenerApplicationResponse apply(UUID userId, ListenerApplicationRequest request) {
        if (repository.hasOpenApplication(userId)) throw new ConflictException("A listener application is already under review");
        repository.createApplication(uuidGenerator.nextUuid(), userId, request);
        return repository.latestApplication(userId).orElseThrow();
    }

    @Transactional(readOnly = true)
    public ListenerApplicationResponse application(UUID userId) {
        return repository.latestApplication(userId).orElseThrow(() -> new NotFoundException("Listener application was not found"));
    }

    @Transactional(readOnly = true)
    public ListenerTrainingResponse training(UUID userId) {
        if (!repository.hasApplication(userId)) throw new NotFoundException("Submit a listener application before training");
        List<ListenerTrainingModuleResponse> modules = repository.trainingModules();
        List<String> completed = repository.completedTrainingModules(userId);
        String status = repository.latestApplication(userId).map(ListenerApplicationResponse::status).orElse("NOT_SUBMITTED");
        return new ListenerTrainingResponse(modules, completed, modules.size(), completed.size() == modules.size(),
                status, repository.listenerActivated(userId));
    }

    @Transactional
    public ListenerTrainingResponse completeTrainingModule(UUID userId, String moduleKey) {
        if (!repository.trainingModuleExists(moduleKey)) throw new IllegalArgumentException("Unknown listener training module");
        if (!repository.hasApplication(userId)) throw new NotFoundException("Submit a listener application before training");
        repository.completeTrainingModule(userId, moduleKey);
        return training(userId);
    }
}
