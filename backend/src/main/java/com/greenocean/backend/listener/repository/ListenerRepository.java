package com.greenocean.backend.listener.repository;

import com.greenocean.backend.listener.dto.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ListenerRepository {
    private final JdbcTemplate jdbcTemplate;
    public ListenerRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public List<ListenerResponse> listeners(UUID viewerId, boolean onlineOnly, String topic) {
        String topicFilter = topic == null || topic.isBlank() || "all".equalsIgnoreCase(topic) ? null : topic.trim().toLowerCase();
        return jdbcTemplate.query("""
                SELECT l.user_id,l.display_title,l.listening_style,l.languages,ARRAY[]::TEXT[] AS topics,l.completed_sessions,l.safety_score,live_listener_ready(l.user_id) AS online, p.display_name, p.username, p.avatar_url
                  FROM listener_profiles l JOIN profiles p ON p.user_id = l.user_id
                 WHERE l.status = 'ACTIVE' AND live_listener_qualified(l.user_id) AND l.user_id <> ?
                   AND (? = FALSE OR (live_listener_ready(l.user_id) AND listener_capacity_available(?,l.user_id,NULL) AND NOT EXISTS(SELECT 1 FROM live_support_requests r WHERE (r.listener_id=l.user_id OR r.member_id=l.user_id) AND r.status IN ('REQUESTED','ACCEPTED','ACTIVE','INTERRUPTED'))))
                   AND (CAST(? AS TEXT) IS NULL OR EXISTS (SELECT 1 FROM unnest(l.topics) t
                                                           WHERE LOWER(t) LIKE '%' || CAST(? AS TEXT) || '%'))
                   AND NOT EXISTS (SELECT 1 FROM blocks b
                                    WHERE (b.blocker_id = ? AND b.blocked_id = l.user_id)
                                       OR (b.blocker_id = l.user_id AND b.blocked_id = ?))
                 ORDER BY live_listener_ready(l.user_id) DESC,l.user_id
                """, this::map, viewerId, onlineOnly, viewerId, topicFilter, topicFilter, viewerId, viewerId);
    }

    public Optional<ListenerResponse> listener(UUID id, UUID viewerId) {
        return listeners(viewerId, false, null).stream().filter(item -> item.id().equals(id)).findFirst();
    }

    public boolean hasOpenApplication(UUID userId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                SELECT EXISTS(SELECT 1 FROM listener_applications
                               WHERE user_id = ? AND status IN ('SUBMITTED','IN_REVIEW'))
                """, Boolean.class, userId));
    }

    public void createApplication(UUID id, UUID userId, ListenerApplicationRequest request) {
        jdbcTemplate.update("""
                INSERT INTO listener_applications
                    (id, user_id, motivation, experience, weekly_availability, accepted_code)
                VALUES (?, ?, ?, ?, ?, ?)
                """, id, userId, request.motivation().trim(), request.experience().trim(),
                request.weeklyAvailability().trim(), request.acceptedCode());
    }

    public Optional<ListenerApplicationResponse> latestApplication(UUID userId) {
        return jdbcTemplate.query("""
                SELECT id, motivation, experience, weekly_availability, status, review_note, submitted_at, reviewed_at
                  FROM listener_applications WHERE user_id = ? ORDER BY submitted_at DESC LIMIT 1
                """, (rs, row) -> {
            OffsetDateTime reviewed = rs.getObject("reviewed_at", OffsetDateTime.class);
            return new ListenerApplicationResponse(rs.getObject("id", UUID.class), rs.getString("motivation"),
                    rs.getString("experience"), rs.getString("weekly_availability"), rs.getString("status"),
                    rs.getString("review_note"), rs.getObject("submitted_at", OffsetDateTime.class).toInstant(),
                    reviewed == null ? null : reviewed.toInstant());
        }, userId).stream().findFirst();
    }

    public boolean hasApplication(UUID userId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM listener_applications WHERE user_id = ?)", Boolean.class, userId));
    }

    public List<String> completedTrainingModules(UUID userId) {
        return jdbcTemplate.query("""
                SELECT module_key FROM listener_training_progress WHERE user_id = ? ORDER BY completed_at
                """, (rs, row) -> rs.getString("module_key"), userId);
    }

    public List<ListenerTrainingModuleResponse> trainingModules() {
        return jdbcTemplate.query("""
                SELECT module_key, title, description, sort_order
                  FROM listener_training_modules
                 WHERE is_active = TRUE
                 ORDER BY sort_order, module_key
                """, (rs, row) -> new ListenerTrainingModuleResponse(
                rs.getString("module_key"), rs.getString("title"), rs.getString("description"),
                rs.getShort("sort_order")));
    }

    public boolean trainingModuleExists(String moduleKey) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                SELECT EXISTS(SELECT 1 FROM listener_training_modules WHERE module_key = ? AND is_active = TRUE)
                """, Boolean.class, moduleKey));
    }

    public void completeTrainingModule(UUID userId, String moduleKey) {
        jdbcTemplate.update("""
                INSERT INTO listener_training_progress (user_id, module_key) VALUES (?, ?) ON CONFLICT DO NOTHING
                """, userId, moduleKey);
    }

    public boolean listenerActivated(UUID userId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                SELECT EXISTS(SELECT 1 FROM listener_profiles WHERE user_id = ? AND status = 'ACTIVE')
                """, Boolean.class, userId));
    }

    private ListenerResponse map(ResultSet rs, int row) throws SQLException {
        return new ListenerResponse(rs.getObject("user_id", UUID.class), rs.getString("display_name"),
                rs.getString("username"), rs.getString("avatar_url"), rs.getString("display_title"),
                rs.getString("listening_style"), strings(rs.getArray("languages")), strings(rs.getArray("topics")),
                rs.getInt("completed_sessions"), rs.getInt("safety_score"), rs.getBoolean("online"));
    }
    private List<String> strings(Array array) throws SQLException {
        return array == null ? List.of() : Arrays.asList((String[]) array.getArray());
    }
}
