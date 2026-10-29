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
        if("PROFESSIONAL_CARE".equals(service))throw new ForbiddenException("Legacy professional conversations are read-only; use the bounded clinic encounter");
        if("LISTENER".equals(service))live.requireSending(conversationId,userId,clientMessageId);
        UUID id = repository.send(uuidGenerator.nextUuid(), conversationId, userId, body.trim(),clientMessageId);
        return repository.messages(conversationId, userId).stream()
                .filter(message -> message.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Created message could not be read"));
    }

    @Transactional
    public UUID startProfessionalConversation(UUID professionalId, UUID userId) {
        throw new ForbiddenException("Professional care uses an explicitly consented, bounded clinic encounter; generic professional messaging cannot start care");
    }

    @Transactional
    public UUID startDirectConversation(UUID otherUserId, UUID userId) {
        access.require(otherUserId,"COMMUNITY");
        if(otherUserId.equals(userId))throw new IllegalArgumentException("Choose another member");
        String pair=userId.compareTo(otherUserId)<0?userId+":"+otherUserId:otherUserId+":"+userId;
        db.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,0))","relationship:"+pair);
        if (Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM blocks WHERE (blocker_id=? AND blocked_id=?) OR (blocker_id=? AND blocked_id=?))",Boolean.class,userId,otherUserId,otherUserId,userId))) throw new ForbiddenException("Conversation is not available");
        var previous=db.queryForList("SELECT id,request_status FROM conversations WHERE conversation_kind='DIRECT' AND service_kind='COMMUNITY' AND ((requester_id=? AND request_target_id=?) OR (requester_id=? AND request_target_id=?)) AND request_status<>'ACCEPTED' ORDER BY updated_at DESC LIMIT 1",userId,otherUserId,otherUserId,userId);
        if(!previous.isEmpty()) {
            if(!repository.canStartDirectConversation(userId,otherUserId))throw new ForbiddenException("This account is not accepting message requests from you");
            if(!"PENDING".equals(previous.getFirst().get("request_status")))throw new com.greenocean.backend.common.exception.ConflictException("This request is closed; another request is not available");
            return (UUID)previous.getFirst().get("id");
        }
        return repository.directConversation(userId, otherUserId).orElseGet(() -> {
            if (!repository.canStartDirectConversation(userId, otherUserId)) {
                throw new ForbiddenException("This account is not accepting message requests from you");
            }
            UUID id = uuidGenerator.nextUuid();
            repository.createDirectConversation(id, uuidGenerator.nextUuid(), userId, otherUserId);
            boolean trusted=Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM follows WHERE follower_id=? AND following_id=?)",Boolean.class,otherUserId,userId));
            db.update("UPDATE conversations SET requester_id=?,request_target_id=?,request_status=? WHERE id=?",userId,otherUserId,trusted?"ACCEPTED":"PENDING",id);
            if(!trusted)db.update("INSERT INTO notifications(user_id,actor_user_id,type) SELECT ?,?,'MESSAGE_REQUEST' WHERE COALESCE((SELECT notify_messages FROM user_preferences WHERE user_id=?),TRUE)",otherUserId,userId,otherUserId);
            return id;
        });
    }

    public java.util.Map<String,Boolean> directPermission(UUID owner,UUID target) {
        if(owner.equals(target))return java.util.Map.of("allowed",false);
        access.require(target,"COMMUNITY");
        if(Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM blocks WHERE (blocker_id=? AND blocked_id=?) OR(blocker_id=? AND blocked_id=?))",Boolean.class,owner,target,target,owner)))return java.util.Map.of("allowed",false);
        return java.util.Map.of("allowed",repository.directConversation(owner,target).isPresent()||repository.canStartDirectConversation(owner,target));
    }
    public String requestStatus(UUID id,UUID owner) {
        return db.queryForList("SELECT request_status FROM conversations c WHERE c.id=? AND EXISTS(SELECT 1 FROM conversation_members m WHERE m.conversation_id=c.id AND m.user_id=?)",String.class,id,owner).stream().findFirst().orElseThrow(()->new NotFoundException("Conversation was not found"));
    }
    public java.util.List<java.util.Map<String,Object>> requests(UUID owner) {
        return db.queryForList("SELECT c.id,c.request_status AS status,c.requester_id,c.request_target_id,c.updated_at,p.display_name,p.username FROM conversations c JOIN profiles p ON p.user_id=CASE WHEN c.requester_id=? THEN c.request_target_id ELSE c.requester_id END JOIN users u ON u.id=p.user_id AND u.status='ACTIVE' WHERE c.service_kind='COMMUNITY' AND c.conversation_kind='DIRECT' AND c.request_status<>'ACCEPTED' AND (c.requester_id=? OR c.request_target_id=?) AND NOT EXISTS(SELECT 1 FROM blocks b WHERE (b.blocker_id=c.requester_id AND b.blocked_id=c.request_target_id) OR(b.blocker_id=c.request_target_id AND b.blocked_id=c.requester_id)) ORDER BY c.updated_at DESC,c.id DESC LIMIT 50",owner,owner,owner);
    }
    @Transactional
    public void resolveRequest(UUID id,UUID owner,String action) {
        var rows=db.queryForList("SELECT * FROM conversations WHERE id=? AND service_kind='COMMUNITY' AND conversation_kind='DIRECT' AND request_status='PENDING' FOR UPDATE",id);
        if(rows.isEmpty())throw new NotFoundException("Pending request was not found");
        var row=rows.getFirst();UUID from=(UUID)row.get("requester_id"),target=(UUID)row.get("request_target_id");
        if(!java.util.Set.of("ACCEPT","DECLINE","CANCEL").contains(action))throw new IllegalArgumentException("Unsupported request action");
        if(!(action.equals("CANCEL")?owner.equals(from):owner.equals(target)))throw new NotFoundException("Pending request was not found");
        access.require(from,"COMMUNITY");access.require(target,"COMMUNITY");
        if(action.equals("ACCEPT") && !repository.canStartDirectConversation(from,target))throw new ForbiddenException("Request is no longer permitted by current settings");
        if(Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM blocks WHERE (blocker_id=? AND blocked_id=?) OR (blocker_id=? AND blocked_id=?))",Boolean.class,from,target,target,from)))throw new NotFoundException("Pending request was not found");
        db.update("UPDATE conversations SET request_status=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",action.equals("ACCEPT")?"ACCEPTED":action.equals("DECLINE")?"DECLINED":"CANCELLED",id);
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
