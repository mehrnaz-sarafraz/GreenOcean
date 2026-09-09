package com.greenocean.backend.messaging.service;

import com.greenocean.backend.catalog.repository.CatalogRepository;
import com.greenocean.backend.common.exception.ForbiddenException;
import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import com.greenocean.backend.messaging.dto.ConversationResponse;
import com.greenocean.backend.messaging.dto.MessageResponse;
import com.greenocean.backend.messaging.dto.SupportChannelResponse;
import com.greenocean.backend.messaging.dto.SupportAvailabilityResponse;
import com.greenocean.backend.messaging.dto.SendMessageRequest;
import com.greenocean.backend.messaging.repository.MessagingRepository;
import com.greenocean.backend.common.api.PageResponse;
import com.greenocean.backend.common.exception.TooManyRequestsException;
import com.greenocean.backend.common.exception.ConflictException;
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
    public PageResponse<ConversationResponse> conversations(UUID userId, int page, int size) {
        validatePage(page, size, 50);
        return page(repository.conversations(userId, page, size), page, size);
    }

    @Transactional
    public PageResponse<MessageResponse> messages(UUID conversationId, UUID userId, int pageNumber, int size) {
        validatePage(pageNumber, size, 100);
        requireConversation(conversationId, userId);
        List<MessageResponse> messages = repository.messages(conversationId, userId, pageNumber, size);
        repository.markRead(conversationId, userId);
        return page(messages, pageNumber, size);
    }

    @Transactional
    public MessageResponse send(UUID conversationId, UUID userId, SendMessageRequest request) {
        ConversationResponse conversation = requireConversation(conversationId, userId);
        if (!conversation.writable()) throw new ForbiddenException("This announcement channel is read-only");
        if (repository.isBlockedInPrivateConversation(conversationId, userId)) throw new ForbiddenException("Messaging is unavailable for this conversation");
        if (repository.recentMessageCount(userId) >= 30) throw new TooManyRequestsException("Message rate limit exceeded", 60);
        UUID id = uuidGenerator.nextUuid();
        UUID storedId = repository.send(id, conversationId, userId, sanitize(request.body()), request.clientMessageId());
        return repository.message(storedId, conversationId, userId)
                .orElseThrow(() -> new ConflictException("clientMessageId was already used in another conversation"));
    }

    @Transactional
    public void archive(UUID conversationId, UUID userId) {
        requireConversation(conversationId, userId);
        repository.archive(conversationId, userId);
    }

    @Transactional
    public UUID startProfessionalConversation(UUID professionalId, UUID userId) {
        if (!catalogRepository.isVerifiedProfessional(professionalId)) {
            throw new NotFoundException("Professional was not found");
        }
        return repository.professionalConversation(userId, professionalId).orElseGet(() -> {
            UUID id = uuidGenerator.nextUuid();
            repository.createProfessionalConversation(id, uuidGenerator.nextUuid(), userId, professionalId);
            return id;
        });
    }

    @Transactional(readOnly = true)
    public SupportAvailabilityResponse supportAvailability() { return repository.supportAvailability(); }

    @Transactional
    public UUID startListenerConversation(UUID userId) {
        UUID listenerId = repository.availableListener()
                .orElseThrow(() -> new NotFoundException("No trained listener is available right now"));
        return repository.listenerConversation(userId, listenerId).orElseGet(() -> {
            UUID id = uuidGenerator.nextUuid();
            repository.createListenerConversation(id, uuidGenerator.nextUuid(), userId, listenerId);
            return id;
        });
    }

    private SupportChannelResponse channel(UUID id, UUID userId) {
        return repository.channel(id, userId).orElseThrow(() -> new NotFoundException("Support channel was not found"));
    }
    private ConversationResponse requireConversation(UUID id, UUID userId) {
        return repository.conversation(id, userId)
                .orElseThrow(() -> new ForbiddenException("Conversation is not available"));
    }

    private void validatePage(int page, int size, int max) {
        if (page < 0 || size < 1 || size > max) throw new IllegalArgumentException("Invalid pagination parameters");
    }
    private <T> PageResponse<T> page(List<T> rows, int page, int size) {
        boolean hasNext = rows.size() > size;
        return new PageResponse<>(hasNext ? List.copyOf(rows.subList(0, size)) : List.copyOf(rows), page, size, hasNext);
    }
    private String sanitize(String body) {
        String clean = body.replaceAll("[\\p{Cc}&&[^\\n\\t]]", "").trim();
        if (clean.isBlank()) throw new IllegalArgumentException("Message body is empty");
        return clean;
    }
}
