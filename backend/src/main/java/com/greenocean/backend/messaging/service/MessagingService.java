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
    private final com.greenocean.backend.listener.live.LiveSupportService live;
    private final com.greenocean.backend.identity.AccessPolicy access;
    private final org.springframework.jdbc.core.JdbcTemplate db;
    private final MessagingRepository repository;
    private final CatalogRepository catalogRepository;
    private final DatabaseUuidGenerator uuidGenerator;

    public MessagingService(MessagingRepository repository, CatalogRepository catalogRepository,
                            DatabaseUuidGenerator uuidGenerator, com.greenocean.backend.identity.AccessPolicy access, org.springframework.jdbc.core.JdbcTemplate db,com.greenocean.backend.listener.live.LiveSupportService live) {
        this.live=live;
        this.access=access;this.db=db;
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

    @Transactional
    public List<ConversationResponse> conversations(UUID userId) { live.reconcile(); return repository.conversations(userId); }

    @Transactional
    public List<MessageResponse> messages(UUID conversationId, UUID userId) {
        requireConversation(conversationId, userId);
        List<MessageResponse> messages = repository.messages(conversationId, userId);
        repository.markRead(conversationId, userId);
        return messages;
    }

    @Transactional
    public MessageResponse send(UUID conversationId,UUID userId,String body) {return send(conversationId,userId,body,null);}

    @Transactional(noRollbackFor=com.greenocean.backend.listener.live.LiveSupportException.class)
    public MessageResponse send(UUID conversationId, UUID userId, String body, UUID clientMessageId) {
        requireConversation(conversationId, userId);
        String service=db.queryForObject("SELECT service_kind FROM conversations WHERE id=?",String.class,conversationId);
        if("LISTENER".equals(service))live.requireSending(conversationId,userId,clientMessageId);
        UUID id = repository.send(uuidGenerator.nextUuid(), conversationId, userId, body.trim(),clientMessageId);
        return repository.messages(conversationId, userId).stream()
                .filter(message -> message.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Created message could not be read"));
    }

    @Transactional
    public UUID startProfessionalConversation(UUID professionalId, UUID userId) {
        access.requireCare(userId,professionalId);
        if (catalogRepository.professional(professionalId).isEmpty()) {
            throw new NotFoundException("Professional was not found");
        }
        return repository.professionalConversation(userId, professionalId).orElseGet(() -> {
            UUID id = uuidGenerator.nextUuid();
            repository.createProfessionalConversation(id, uuidGenerator.nextUuid(), userId, professionalId);
            return id;
        });
    }

    @Transactional
    public UUID startDirectConversation(UUID otherUserId, UUID userId) {
        access.require(otherUserId,"COMMUNITY");
        if (Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM blocks WHERE (blocker_id=? AND blocked_id=?) OR (blocker_id=? AND blocked_id=?))",Boolean.class,userId,otherUserId,otherUserId,userId))) throw new ForbiddenException("Conversation is not available");
        return repository.directConversation(userId, otherUserId).orElseGet(() -> {
            if (!repository.canStartDirectConversation(userId, otherUserId)) {
                throw new ForbiddenException("This account is not accepting message requests from you");
            }
            UUID id = uuidGenerator.nextUuid();
            repository.createDirectConversation(id, uuidGenerator.nextUuid(), userId, otherUserId);
            return id;
        });
    }

    private SupportChannelResponse channel(UUID id, UUID userId) {
        return repository.channel(id, userId).orElseThrow(() -> new NotFoundException("Support channel was not found"));
    }
    private void requireConversation(UUID id, UUID userId) {
        live.reconcile();
        if (repository.conversation(id, userId).isEmpty()) throw new ForbiddenException("Conversation is not available");
        String service=db.queryForList("SELECT service_kind FROM conversations WHERE id=?",String.class,id).stream().findFirst().orElse("COMMUNITY");
        access.require(userId,service);
        if("PROFESSIONAL_CARE".equals(service)) {
            UUID professional=db.queryForObject("SELECT professional_user_id FROM conversations WHERE id=?",UUID.class,id);
            if(professional==null) throw new ForbiddenException("Professional conversation is unavailable");
            UUID requester=professional.equals(userId)?db.queryForObject("SELECT user_id FROM conversation_members WHERE conversation_id=? AND user_id<>? LIMIT 1",UUID.class,id,professional):userId;
            access.requireCare(requester,professional);
        }
    }
}
