package com.greenocean.backend.messaging.repository;

import com.greenocean.backend.messaging.dto.ConversationResponse;
import com.greenocean.backend.messaging.dto.MessageResponse;
import com.greenocean.backend.messaging.dto.SupportChannelResponse;
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

    public List<ConversationResponse> conversations(UUID userId) {
        return jdbcTemplate.query("""
                SELECT c.id, c.title, c.subtitle, c.conversation_kind,
                       pp.verification_status,
                       last_message.body AS last_message, last_message.created_at AS last_message_at,
                       (SELECT COUNT(*) FROM messages unread_messages
                         WHERE unread_messages.conversation_id = c.id
                           AND unread_messages.created_at > COALESCE(cm.last_read_at, cm.joined_at)
                           AND unread_messages.sender_id IS DISTINCT FROM ?) AS unread
                  FROM conversation_members cm
                  JOIN conversations c ON c.id = cm.conversation_id AND c.is_active = TRUE
                  LEFT JOIN professional_profiles pp ON pp.user_id = c.professional_user_id
                  LEFT JOIN LATERAL (
                      SELECT m.body, m.created_at FROM messages m
                       WHERE m.conversation_id = c.id AND m.deleted_at IS NULL
                       ORDER BY m.created_at DESC LIMIT 1
                  ) last_message ON TRUE
                 WHERE cm.user_id = ?
                 ORDER BY COALESCE(last_message.created_at, c.updated_at) DESC
                """, (rs, row) -> new ConversationResponse(
                rs.getObject("id", UUID.class), rs.getString("title"), rs.getString("subtitle"),
                rs.getString("last_message"), instant(rs.getObject("last_message_at", OffsetDateTime.class)),
                rs.getLong("unread"), "VERIFIED".equals(rs.getString("verification_status")),
                rs.getString("conversation_kind"), false), userId, userId);
    }

    public Optional<ConversationResponse> conversation(UUID conversationId, UUID userId) {
        return conversations(userId).stream().filter(item -> item.id().equals(conversationId)).findFirst();
    }

    public List<MessageResponse> messages(UUID conversationId, UUID userId) {
        return jdbcTemplate.query("""
                SELECT m.id, m.sender_id, p.display_name, m.body, m.message_kind, m.created_at
                  FROM messages m
                  LEFT JOIN profiles p ON p.user_id = m.sender_id
                 WHERE m.conversation_id = ? AND m.deleted_at IS NULL
                 ORDER BY m.created_at, m.id
                """, (rs, row) -> {
            UUID senderId = rs.getObject("sender_id", UUID.class);
            return new MessageResponse(
                    rs.getObject("id", UUID.class), senderId, rs.getString("display_name"), rs.getString("body"),
                    rs.getObject("created_at", OffsetDateTime.class).toInstant(), userId.equals(senderId),
                    "SYSTEM".equals(rs.getString("message_kind")));
        }, conversationId);
    }

    public void markRead(UUID conversationId, UUID userId) {
        jdbcTemplate.update("UPDATE conversation_members SET last_read_at = CURRENT_TIMESTAMP WHERE conversation_id = ? AND user_id = ?",
                conversationId, userId);
    }

    public void send(UUID id, UUID conversationId, UUID userId, String body) {
        jdbcTemplate.update("""
                INSERT INTO messages (id, conversation_id, sender_id, message_kind, body) VALUES (?, ?, ?, 'USER', ?)
                """, id, conversationId, userId, body);
        jdbcTemplate.update("UPDATE conversations SET updated_at = CURRENT_TIMESTAMP WHERE id = ?", conversationId);
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

    private java.time.Instant instant(OffsetDateTime value) { return value == null ? null : value.toInstant(); }
}
