package com.greenocean.backend.messaging.service;

import com.greenocean.backend.catalog.repository.CatalogRepository;
import com.greenocean.backend.common.exception.ForbiddenException;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import com.greenocean.backend.messaging.dto.ConversationResponse;
import com.greenocean.backend.messaging.dto.MessageResponse;
import com.greenocean.backend.messaging.dto.SupportChannelResponse;
import com.greenocean.backend.messaging.repository.MessagingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MessagingService {
    private final MessagingRepository repository;
    private final CatalogRepository catalogRepository;
    private final DatabaseUuidGenerator uuidGenerator;

    public MessagingService(MessagingRepository repository, CatalogRepository catalogRepository,
                            DatabaseUuidGenerator uuidGenerator) {
        this.repository = repository;
        this.catalogRepository = catalogRepository;
        this.uuidGenerator = uuidGenerator;
    }

    @Transactional(readOnly = true)
    public List<SupportChannelResponse> channels(UUID userId) { return repository.channels(userId); }

    @Transactional
    public SupportChannelResponse join(UUID channelId, UUID userId) {
        SupportChannelResponse channel = channel(channelId, userId);
        if (channel.conversationId() == null) throw new IllegalStateException("Channel conversation is not configured");
        repository.joinChannel(channel.conversationId(), userId);
        return channel(channelId, userId);
    }

    @Transactional
    public void leave(UUID channelId, UUID userId) {
        SupportChannelResponse channel = channel(channelId, userId);
        if (channel.conversationId() != null) repository.leaveChannel(channel.conversationId(), userId);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> conversations(UUID userId) { return repository.conversations(userId); }

    @Transactional
    public List<MessageResponse> messages(UUID conversationId, UUID userId) {
        requireConversation(conversationId, userId);
        List<MessageResponse> messages = repository.messages(conversationId, userId);
        repository.markRead(conversationId, userId);
        return messages;
    }

    @Transactional
    public MessageResponse send(UUID conversationId, UUID userId, String body) {
        requireConversation(conversationId, userId);
        UUID id = uuidGenerator.nextUuid();
        repository.send(id, conversationId, userId, body.trim());
        return repository.messages(conversationId, userId).stream()
                .filter(message -> message.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Created message could not be read"));
    }

    @Transactional
    public UUID startProfessionalConversation(UUID professionalId, UUID userId) {
        if (catalogRepository.professional(professionalId).isEmpty()) {
            throw new NotFoundException("Professional was not found");
        }
        return repository.professionalConversation(userId, professionalId).orElseGet(() -> {
            UUID id = uuidGenerator.nextUuid();
            repository.createProfessionalConversation(id, uuidGenerator.nextUuid(), userId, professionalId);
            return id;
        });
    }

    private SupportChannelResponse channel(UUID id, UUID userId) {
        return repository.channel(id, userId).orElseThrow(() -> new NotFoundException("Support channel was not found"));
    }
    private void requireConversation(UUID id, UUID userId) {
        if (repository.conversation(id, userId).isEmpty()) throw new ForbiddenException("Conversation is not available");
    }
}
