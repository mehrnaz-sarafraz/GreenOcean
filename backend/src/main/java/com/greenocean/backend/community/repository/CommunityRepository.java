package com.greenocean.backend.community.repository;

import com.greenocean.backend.community.dto.CommunityResponse;
import com.greenocean.backend.community.dto.CommunityDetailsResponse;
import com.greenocean.backend.community.dto.CommunityJoinRequestResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CommunityRepository {
    private static final String SELECT = """
            SELECT c.id, c.name, c.slug, c.description, c.icon_url, c.is_private, c.created_at,c.privacy_type,c.status,c.rules_version,
                   (SELECT jsonb_agg(rule_text ORDER BY position) FROM community_rules WHERE community_id=c.id)::text AS rules_json,
                   NULL::bigint AS member_count,
                   EXISTS(SELECT 1 FROM community_members mine WHERE mine.community_id = c.id AND mine.user_id = ?) AS member,
                   (SELECT mine.role FROM community_members mine WHERE mine.community_id = c.id AND mine.user_id = ?) AS membership_role
                   ,EXISTS(SELECT 1 FROM community_join_requests request
                            WHERE request.community_id = c.id AND request.requester_id = ? AND request.status = 'PENDING') AS join_requested
              FROM communities c
             WHERE visible_group(c.id, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public CommunityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(UUID id, UUID ownerId, String name, String slug, String description, boolean privateCommunity) {
        try {
            jdbcTemplate.update("""
                    INSERT INTO communities (id, name, slug, description, is_private, created_by)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, id, name, slug, description, privateCommunity, ownerId);
            jdbcTemplate.update("""
                    INSERT INTO community_members (community_id, user_id, role)
                    VALUES (?, ?, 'OWNER')
                    """, id, ownerId);
            insertDefaultRule(id, 1, "Share lived experience; do not diagnose other members.");
            insertDefaultRule(id, 2, "No harassment, hate, threats, spam, or unsafe treatment claims.");
            insertDefaultRule(id, 3, "Protect privacy and use content notes for distressing experiences.");
        } catch (DuplicateKeyException exception) {
            throw new IllegalStateException("A community with this name or slug already exists", exception);
        }
    }

    public Optional<CommunityResponse> findById(UUID communityId, UUID viewerId) {
        return jdbcTemplate.query(SELECT + " AND c.id = ?", this::map,
                viewerId, viewerId, viewerId, viewerId, communityId).stream().findFirst();
    }

    public Optional<CommunityResponse> findBySlug(String slug, UUID viewerId) {
        return jdbcTemplate.query(SELECT + " AND c.slug = ?", this::map,
                viewerId, viewerId, viewerId, viewerId, slug).stream().findFirst();
    }

    public List<CommunityResponse> findAll(UUID viewerId, String query, int page, int size) {
        String term = "%" + query.trim().toLowerCase() + "%";
        String sql = SELECT + """
                 AND c.privacy_type<>'HIDDEN' AND (LOWER(c.name) LIKE ? OR LOWER(COALESCE(c.description, '')) LIKE ? OR LOWER(c.slug) LIKE ?)
                 ORDER BY c.created_at DESC,c.id DESC
                 LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql, this::map, viewerId, viewerId, viewerId, viewerId, term, term, term,
                size + 1, (long) page * size);
    }

    public int join(UUID communityId, UUID userId) {
        return jdbcTemplate.update("""
                INSERT INTO community_members (community_id, user_id, role)
                VALUES (?, ?, 'MEMBER')
                ON CONFLICT DO NOTHING
                """, communityId, userId);
    }

    public boolean requestJoin(UUID communityId, UUID userId) {
        return jdbcTemplate.update("""
                INSERT INTO community_join_requests (community_id, requester_id, status, requested_at, resolved_at, resolved_by)
                VALUES (?, ?, 'PENDING', CURRENT_TIMESTAMP, NULL, NULL)
                ON CONFLICT (community_id, requester_id) DO UPDATE
                    SET status = 'PENDING', requested_at = CURRENT_TIMESTAMP, resolved_at = NULL, resolved_by = NULL
                  WHERE community_join_requests.status <> 'PENDING'
                """, communityId, userId) == 1;
    }

    public void cancelJoinRequest(UUID communityId, UUID userId) {
        jdbcTemplate.update("""
                UPDATE community_join_requests SET status = 'CANCELLED', resolved_at = CURRENT_TIMESTAMP
                 WHERE community_id = ? AND requester_id = ? AND status = 'PENDING'
                """, communityId, userId);
    }

    public List<CommunityJoinRequestResponse> joinRequests(UUID communityId) {
        return jdbcTemplate.query("""
                SELECT p.user_id, p.display_name, p.username, p.avatar_url, r.requested_at
                  FROM community_join_requests r JOIN profiles p ON p.user_id = r.requester_id
                 WHERE r.community_id = ? AND r.status = 'PENDING'
                 ORDER BY r.requested_at
                """, (rs, row) -> new CommunityJoinRequestResponse(
                rs.getObject("user_id", UUID.class), rs.getString("display_name"), rs.getString("username"),
                rs.getString("avatar_url"), rs.getObject("requested_at", OffsetDateTime.class).toInstant()), communityId);
    }

    public boolean resolveJoinRequest(UUID communityId, UUID requesterId, UUID resolverId, boolean approved) {
        int updated = jdbcTemplate.update("""
                UPDATE community_join_requests
                   SET status = ?, resolved_at = CURRENT_TIMESTAMP, resolved_by = ?
                 WHERE community_id = ? AND requester_id = ? AND status = 'PENDING'
                """, approved ? "APPROVED" : "DECLINED", resolverId, communityId, requesterId);
        if (updated == 1 && approved) join(communityId, requesterId);
        return updated == 1;
    }

    public void replaceRules(UUID communityId, List<String> rules) {
        jdbcTemplate.update("DELETE FROM community_rules WHERE community_id = ?", communityId);
        for (int index = 0; index < rules.size(); index++) insertDefaultRule(communityId, index + 1, rules.get(index));
    }

    public void createEvent(UUID id, UUID communityId, UUID hostId, String title, String description,
                            String format, java.time.Instant startsAt, java.time.Instant endsAt, Integer capacity) {
        jdbcTemplate.update("""
                INSERT INTO community_events
                    (id, community_id, host_user_id, title, description, format, starts_at, ends_at, capacity)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, id, communityId, hostId, title, description, format,
                java.time.OffsetDateTime.ofInstant(startsAt, java.time.ZoneOffset.UTC),
                java.time.OffsetDateTime.ofInstant(endsAt, java.time.ZoneOffset.UTC), capacity);
    }

    public boolean cancelEvent(UUID communityId, UUID eventId) {
        return jdbcTemplate.update("""
                UPDATE community_events SET status = 'CANCELLED'
                 WHERE id = ? AND community_id = ? AND status IN ('SCHEDULED', 'LIVE')
                """, eventId, communityId) == 1;
    }

    public boolean updateMemberRole(UUID communityId, UUID memberId, String role) {
        return jdbcTemplate.update("""
                UPDATE community_members SET role = ?
                 WHERE community_id = ? AND user_id = ? AND role <> 'OWNER'
                """, role, communityId, memberId) == 1;
    }

    public boolean removeMember(UUID communityId, UUID memberId) {
        return jdbcTemplate.update("""
                DELETE FROM community_members
                 WHERE community_id = ? AND user_id = ? AND role <> 'OWNER'
                """, communityId, memberId) == 1;
    }

    public String membershipRole(UUID communityId, UUID userId) {
        List<String> roles = jdbcTemplate.query("""
                SELECT role FROM community_members WHERE community_id = ? AND user_id = ?
                """, (resultSet, row) -> resultSet.getString("role"), communityId, userId);
        return roles.stream().findFirst().orElse(null);
    }

    public boolean isMember(UUID communityId, UUID userId) {
        Boolean member = jdbcTemplate.queryForObject("""
                SELECT EXISTS(
                    SELECT 1 FROM community_members WHERE community_id = ? AND user_id = ?
                )
                """, Boolean.class, communityId, userId);
        return Boolean.TRUE.equals(member);
    }

    public CommunityDetailsResponse details(UUID communityId, UUID viewerId) {
        List<CommunityDetailsResponse.Rule> rules = jdbcTemplate.query("""
                SELECT id, position, rule_text
                  FROM community_rules
                 WHERE community_id = ?
                 ORDER BY position
                """, (resultSet, row) -> new CommunityDetailsResponse.Rule(
                resultSet.getObject("id", UUID.class), resultSet.getInt("position"), resultSet.getString("rule_text")
        ), communityId);

        List<CommunityDetailsResponse.Member> members = jdbcTemplate.query("""
                SELECT cm.user_id, p.display_name, p.username, p.avatar_url, cm.role, cm.joined_at
                  FROM community_members cm
                  JOIN profiles p ON p.user_id = cm.user_id
                 WHERE cm.community_id = ? AND EXISTS(SELECT 1 FROM community_members manager
                   WHERE manager.community_id=cm.community_id AND manager.user_id=? AND manager.role IN('OWNER','MODERATOR'))
                 ORDER BY CASE cm.role WHEN 'OWNER' THEN 1 WHEN 'MODERATOR' THEN 2 ELSE 3 END,
                          LOWER(p.display_name)
                """, (resultSet, row) -> new CommunityDetailsResponse.Member(
                resultSet.getObject("user_id", UUID.class), resultSet.getString("display_name"),
                resultSet.getString("username"), resultSet.getString("avatar_url"), resultSet.getString("role"),
                resultSet.getObject("joined_at", OffsetDateTime.class).toInstant()
        ), communityId,viewerId);

        List<CommunityDetailsResponse.Event> events = jdbcTemplate.query("""
                SELECT e.id, e.title, e.description, e.format, p.display_name AS host_name,
                       e.starts_at, e.ends_at, e.capacity, e.status,
                       COUNT(a.user_id) AS attendee_count,
                       BOOL_OR(a.user_id = ?) AS joined
                  FROM community_events e
                  LEFT JOIN profiles p ON p.user_id = e.host_user_id
                  LEFT JOIN community_event_attendees a ON a.event_id = e.id
                 WHERE e.community_id = ? AND e.status <> 'CANCELLED'
                 GROUP BY e.id, p.display_name
                 ORDER BY e.starts_at
                """, (resultSet, row) -> new CommunityDetailsResponse.Event(
                resultSet.getObject("id", UUID.class), resultSet.getString("title"), resultSet.getString("description"),
                resultSet.getString("format"), resultSet.getString("host_name"),
                resultSet.getObject("starts_at", OffsetDateTime.class).toInstant(),
                resultSet.getObject("ends_at", OffsetDateTime.class).toInstant(),
                resultSet.getObject("capacity", Integer.class), resultSet.getLong("attendee_count"),
                resultSet.getBoolean("joined"), resultSet.getString("status")
        ), viewerId, communityId);

        return new CommunityDetailsResponse(rules, members, events);
    }

    public boolean eventBelongsToCommunity(UUID eventId, UUID communityId) {
        Boolean exists = jdbcTemplate.queryForObject("""
                SELECT EXISTS(SELECT 1 FROM community_events WHERE id = ? AND community_id = ? AND status = 'SCHEDULED')
                """, Boolean.class, eventId, communityId);
        return Boolean.TRUE.equals(exists);
    }

    public boolean isEventAttendee(UUID eventId, UUID userId) {
        Boolean exists = jdbcTemplate.queryForObject("""
                SELECT EXISTS(SELECT 1 FROM community_event_attendees WHERE event_id = ? AND user_id = ?)
                """, Boolean.class, eventId, userId);
        return Boolean.TRUE.equals(exists);
    }

    public int joinEvent(UUID eventId, UUID userId) {
        return jdbcTemplate.update("""
                INSERT INTO community_event_attendees (event_id, user_id)
                SELECT e.id, ?
                  FROM community_events e
                 WHERE e.id = ?
                   AND e.status = 'SCHEDULED'
                   AND (e.capacity IS NULL OR
                        (SELECT COUNT(*) FROM community_event_attendees a WHERE a.event_id = e.id) < e.capacity)
                ON CONFLICT DO NOTHING
                """, userId, eventId);
    }

    public void leaveEvent(UUID eventId, UUID userId) {
        jdbcTemplate.update("DELETE FROM community_event_attendees WHERE event_id = ? AND user_id = ?", eventId, userId);
    }

    public void leave(UUID communityId, UUID userId) {
        jdbcTemplate.update("DELETE FROM community_members WHERE community_id = ? AND user_id = ?", communityId, userId);
    }

    private void insertDefaultRule(UUID communityId, int position, String text) {
        jdbcTemplate.update("""
                INSERT INTO community_rules (community_id, position, rule_text) VALUES (?, ?, ?)
                """, communityId, position, text);
    }

    private CommunityResponse map(ResultSet resultSet, int rowNumber) throws SQLException {
        return new CommunityResponse(
                resultSet.getObject("id", UUID.class), resultSet.getString("name"), resultSet.getString("slug"),
                resultSet.getString("description"), resultSet.getString("icon_url"), resultSet.getBoolean("is_private"),
                resultSet.getObject("member_count",Long.class), resultSet.getBoolean("member"), resultSet.getString("membership_role"),
                resultSet.getBoolean("join_requested"),
                resultSet.getObject("created_at", OffsetDateTime.class).toInstant(),
                resultSet.getString("privacy_type"),resultSet.getString("status"),resultSet.getLong("rules_version"),resultSet.getString("rules_json")
        );
    }
}
