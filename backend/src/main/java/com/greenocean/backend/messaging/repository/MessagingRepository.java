package com.greenocean.backend.messaging.repository;

import com.greenocean.backend.messaging.dto.ConversationResponse;
import com.greenocean.backend.messaging.dto.MessageResponse;
import com.greenocean.backend.messaging.dto.SupportChannelResponse;
import com.greenocean.backend.messaging.dto.SupportAvailabilityResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MessagingRepository {
    private final JdbcTemplate jdbcTemplate;
    public MessagingRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public List<SupportChannelResponse> channels(UUID userId) {
        return jdbcTemplate.query("""
                SELECT ch.id, c.id AS conversation_id, ch.name, ch.slug, ch.description, ch.category, ch.icon,
                       ch.channel_type, ch.moderated, ch.next_event,
                       (SELECT COUNT(*) FROM conversation_members all_members WHERE all_members.conversation_id = c.id) AS member_count,
                       EXISTS(SELECT 1 FROM conversation_members mine WHERE mine.conversation_id = c.id AND mine.user_id = ?) AS joined
                  FROM support_channels ch
                  LEFT JOIN conversations c ON c.channel_id = ch.id AND c.is_active = TRUE
                 WHERE ch.is_active = TRUE
                 ORDER BY ch.channel_type, ch.name
                """, (rs, row) -> new SupportChannelResponse(
                rs.getObject("id", UUID.class), rs.getObject("conversation_id", UUID.class), rs.getString("name"),
                rs.getString("slug"), rs.getString("description"), rs.getString("category"), rs.getString("icon"),
                rs.getLong("member_count"), 0, rs.getBoolean("joined"), rs.getString("channel_type"),
                rs.getBoolean("moderated"), rs.getString("next_event")), userId);
    }

    public Optional<SupportChannelResponse> channel(UUID channelId, UUID userId) {
        return channels(userId).stream().filter(channel -> channel.id().equals(channelId)).findFirst();
    }

    public void joinChannel(UUID conversationId, UUID userId) {
        jdbcTemplate.update("""
                INSERT INTO conversation_members (conversation_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING
                """, conversationId, userId);
    }

    public void leaveChannel(UUID conversationId, UUID userId) {
        jdbcTemplate.update("DELETE FROM conversation_members WHERE conversation_id = ? AND user_id = ? AND member_role = 'MEMBER'",
                conversationId, userId);
    }

    public List<ConversationResponse> conversations(UUID userId, int page, int size) {
        return jdbcTemplate.query("""
                SELECT c.id, c.title, c.subtitle, c.conversation_kind,
                       pp.verification_status,
                       CASE WHEN c.conversation_kind = 'LISTENER' THEN lp.status = 'AVAILABLE' ELSE FALSE END AS online,
                       CASE WHEN ch.channel_type = 'ANNOUNCEMENT' THEN FALSE ELSE TRUE END AS writable,
                       last_message.body AS last_message, last_message.created_at AS last_message_at,
                       (SELECT COUNT(*) FROM messages unread_messages
                         WHERE unread_messages.conversation_id = c.id
                           AND unread_messages.created_at > COALESCE(cm.last_read_at, cm.joined_at)
                           AND unread_messages.sender_id IS DISTINCT FROM ?) AS unread
                  FROM conversation_members cm
                  JOIN conversations c ON c.id = cm.conversation_id AND c.is_active = TRUE
                  LEFT JOIN professional_profiles pp ON pp.user_id = c.professional_user_id
                  LEFT JOIN listener_profiles lp ON lp.user_id = c.listener_user_id
                  LEFT JOIN support_channels ch ON ch.id = c.channel_id
                  LEFT JOIN LATERAL (
                      SELECT m.body, m.created_at FROM messages m
                       WHERE m.conversation_id = c.id AND m.deleted_at IS NULL
                       ORDER BY m.created_at DESC LIMIT 1
                  ) last_message ON TRUE
                 WHERE cm.user_id = ? AND cm.archived_at IS NULL
                 ORDER BY COALESCE(last_message.created_at, c.updated_at) DESC
                 LIMIT ? OFFSET ?
                """, (rs, row) -> new ConversationResponse(
                rs.getObject("id", UUID.class), rs.getString("title"), rs.getString("subtitle"),
                rs.getString("last_message"), instant(rs.getObject("last_message_at", OffsetDateTime.class)),
                rs.getLong("unread"), "VERIFIED".equals(rs.getString("verification_status")),
                rs.getString("conversation_kind"), rs.getBoolean("online"), rs.getBoolean("writable")), userId, userId, size + 1, page * size);
    }

    public Optional<ConversationResponse> conversation(UUID conversationId, UUID userId) {
        return jdbcTemplate.query("""
                SELECT c.id, c.title, c.subtitle, c.conversation_kind, pp.verification_status,
                       CASE WHEN c.conversation_kind = 'LISTENER' THEN lp.status = 'AVAILABLE' ELSE FALSE END AS online,
                       CASE WHEN ch.channel_type = 'ANNOUNCEMENT' THEN FALSE ELSE TRUE END AS writable,
                       NULL::TEXT AS last_message, NULL::TIMESTAMPTZ AS last_message_at, 0 AS unread
                  FROM conversation_members cm JOIN conversations c ON c.id = cm.conversation_id AND c.is_active = TRUE
                  LEFT JOIN professional_profiles pp ON pp.user_id = c.professional_user_id
                  LEFT JOIN listener_profiles lp ON lp.user_id = c.listener_user_id
                  LEFT JOIN support_channels ch ON ch.id = c.channel_id
                 WHERE cm.user_id = ? AND c.id = ?
                """, (rs, row) -> new ConversationResponse(rs.getObject("id", UUID.class), rs.getString("title"),
                rs.getString("subtitle"), null, null, 0, "VERIFIED".equals(rs.getString("verification_status")),
                rs.getString("conversation_kind"), rs.getBoolean("online"), rs.getBoolean("writable")), userId, conversationId)
                .stream().findFirst();
    }

    public List<MessageResponse> messages(UUID conversationId, UUID userId, int page, int size) {
        return jdbcTemplate.query("""
                SELECT m.id, m.sender_id, p.display_name, m.body, m.message_kind, m.created_at
                  FROM messages m
                  LEFT JOIN profiles p ON p.user_id = m.sender_id
                 WHERE m.conversation_id = ? AND m.deleted_at IS NULL
                 ORDER BY m.created_at DESC, m.id DESC LIMIT ? OFFSET ?
                """, (rs, row) -> {
            UUID senderId = rs.getObject("sender_id", UUID.class);
            return new MessageResponse(
                    rs.getObject("id", UUID.class), senderId, rs.getString("display_name"), rs.getString("body"),
                    rs.getObject("created_at", OffsetDateTime.class).toInstant(), userId.equals(senderId),
                    "SYSTEM".equals(rs.getString("message_kind")));
        }, conversationId, size + 1, page * size);
    }

    public Optional<MessageResponse> message(UUID id, UUID conversationId, UUID userId) {
        return jdbcTemplate.query("""
                SELECT m.id, m.sender_id, p.display_name, m.body, m.message_kind, m.created_at
                  FROM messages m LEFT JOIN profiles p ON p.user_id = m.sender_id
                 WHERE m.id = ? AND m.conversation_id = ? AND m.deleted_at IS NULL
                """, (rs, row) -> { UUID sender = rs.getObject("sender_id", UUID.class); return new MessageResponse(
                rs.getObject("id", UUID.class), sender, rs.getString("display_name"), rs.getString("body"),
                rs.getObject("created_at", OffsetDateTime.class).toInstant(), userId.equals(sender),
                "SYSTEM".equals(rs.getString("message_kind"))); }, id, conversationId).stream().findFirst();
    }

    public void markRead(UUID conversationId, UUID userId) {
        jdbcTemplate.update("UPDATE conversation_members SET last_read_at = CURRENT_TIMESTAMP WHERE conversation_id = ? AND user_id = ?",
                conversationId, userId);
    }

    public UUID send(UUID id, UUID conversationId, UUID userId, String body, UUID clientMessageId) {
        jdbcTemplate.update("""
                INSERT INTO messages (id, conversation_id, sender_id, message_kind, body, client_message_id)
                VALUES (?, ?, ?, 'USER', ?, ?) ON CONFLICT (sender_id, client_message_id) WHERE sender_id IS NOT NULL AND client_message_id IS NOT NULL DO NOTHING
                """, id, conversationId, userId, body, clientMessageId);
        jdbcTemplate.update("UPDATE conversations SET updated_at = CURRENT_TIMESTAMP WHERE id = ?", conversationId);
        if (clientMessageId == null) return id;
        return jdbcTemplate.queryForObject("SELECT id FROM messages WHERE sender_id = ? AND client_message_id = ?", UUID.class, userId, clientMessageId);
    }

    public long recentMessageCount(UUID userId) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM messages WHERE sender_id = ? AND created_at >= CURRENT_TIMESTAMP - INTERVAL '1 minute'", Long.class, userId);
    }

    public boolean isBlockedInPrivateConversation(UUID conversationId, UUID userId) {
        Boolean blocked = jdbcTemplate.queryForObject("""
                SELECT CASE WHEN c.conversation_kind = 'GROUP' THEN FALSE ELSE EXISTS(
                    SELECT 1 FROM conversation_members other JOIN blocks b
                      ON (b.blocker_id = ? AND b.blocked_id = other.user_id) OR (b.blocked_id = ? AND b.blocker_id = other.user_id)
                     WHERE other.conversation_id = c.id AND other.user_id <> ?)
                END FROM conversations c WHERE c.id = ?
                """, Boolean.class, userId, userId, userId, conversationId);
        return Boolean.TRUE.equals(blocked);
    }

    public void archive(UUID conversationId, UUID userId) {
        jdbcTemplate.update("UPDATE conversation_members SET archived_at = CURRENT_TIMESTAMP WHERE conversation_id = ? AND user_id = ?", conversationId, userId);
    }

    public Optional<UUID> professionalConversation(UUID userId, UUID professionalId) {
        return jdbcTemplate.query("""
                SELECT c.id
                  FROM conversations c
                  JOIN conversation_members cm ON cm.conversation_id = c.id AND cm.user_id = ?
                 WHERE c.conversation_kind = 'PROFESSIONAL' AND c.professional_user_id = ? AND c.is_active = TRUE
                """, (rs, row) -> rs.getObject("id", UUID.class), userId, professionalId).stream().findFirst();
    }

    public void createProfessionalConversation(UUID id, UUID systemMessageId, UUID userId, UUID professionalId) {
        String name = jdbcTemplate.queryForObject("SELECT display_name FROM profiles WHERE user_id = ?", String.class, professionalId);
        String title = jdbcTemplate.queryForObject("SELECT title FROM professional_profiles WHERE user_id = ?", String.class, professionalId);
        jdbcTemplate.update("""
                INSERT INTO conversations (id, conversation_kind, title, subtitle, professional_user_id)
                VALUES (?, 'PROFESSIONAL', ?, ?, ?)
                """, id, name, title, professionalId);
        jdbcTemplate.update("INSERT INTO conversation_members (conversation_id, user_id) VALUES (?, ?), (?, ?)",
                id, userId, id, professionalId);
        jdbcTemplate.update("""
                INSERT INTO messages (id, conversation_id, message_kind, body)
                VALUES (?, ?, 'SYSTEM', 'Professional messages provide general support and do not establish emergency or medical care.')
                """, systemMessageId, id);
    }

    public SupportAvailabilityResponse supportAvailability() {
        return jdbcTemplate.queryForObject("""
                SELECT COALESCE((SELECT SUM(current_capacity - active_conversations)
                                  FROM listener_profiles WHERE status = 'AVAILABLE'), 0) AS listeners,
                       (SELECT COUNT(*) FROM users u
                         WHERE u.status = 'ACTIVE' AND u.deleted_at IS NULL
                           AND u.last_login_at >= CURRENT_TIMESTAMP - INTERVAL '15 minutes') AS peers
                """, (rs, row) -> {
            long listeners = rs.getLong("listeners");
            return new SupportAvailabilityResponse(listeners, rs.getLong("peers"), listeners > 0 ? 60 : 0);
        });
    }

    public Optional<UUID> availableListener() {
        return jdbcTemplate.query("""
                SELECT user_id FROM listener_profiles
                 WHERE status = 'AVAILABLE' AND active_conversations < current_capacity
                 ORDER BY active_conversations, trained_at
                 LIMIT 1 FOR UPDATE SKIP LOCKED
                """, (rs, row) -> rs.getObject("user_id", UUID.class)).stream().findFirst();
    }

    public Optional<UUID> listenerConversation(UUID userId, UUID listenerId) {
        return jdbcTemplate.query("""
                SELECT c.id FROM conversations c
                  JOIN conversation_members cm ON cm.conversation_id = c.id AND cm.user_id = ?
                 WHERE c.conversation_kind = 'LISTENER' AND c.listener_user_id = ? AND c.is_active = TRUE
                """, (rs, row) -> rs.getObject("id", UUID.class), userId, listenerId).stream().findFirst();
    }

    public void createListenerConversation(UUID id, UUID systemMessageId, UUID userId, UUID listenerId) {
        String name = jdbcTemplate.queryForObject("SELECT display_name FROM profiles WHERE user_id = ?", String.class, listenerId);
        jdbcTemplate.update("""
                INSERT INTO conversations (id, conversation_kind, title, subtitle, listener_user_id)
                VALUES (?, 'LISTENER', ?, 'Trained peer listener', ?)
                """, id, name, listenerId);
        jdbcTemplate.update("INSERT INTO conversation_members (conversation_id, user_id) VALUES (?, ?), (?, ?)",
                id, userId, id, listenerId);
        jdbcTemplate.update("""
                INSERT INTO messages (id, conversation_id, message_kind, body)
                VALUES (?, ?, 'SYSTEM', 'Peer listeners provide non-clinical emotional support. GreenOcean is not an emergency service.')
                """, systemMessageId, id);
        jdbcTemplate.update("UPDATE listener_profiles SET active_conversations = active_conversations + 1 WHERE user_id = ?",
                listenerId);
    }

    private java.time.Instant instant(OffsetDateTime value) { return value == null ? null : value.toInstant(); }
}
