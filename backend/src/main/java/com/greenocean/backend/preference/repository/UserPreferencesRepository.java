package com.greenocean.backend.preference.repository;

import com.greenocean.backend.preference.dto.UpdateUserPreferencesRequest;
import com.greenocean.backend.preference.dto.UserPreferencesResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserPreferencesRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserPreferencesRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public Optional<UserPreferencesResponse> find(UUID userId) {
        return jdbcTemplate.query("""
                SELECT * FROM user_preferences WHERE user_id = ?
                """, (rs, row) -> new UserPreferencesResponse(
                strings(rs.getArray("support_topics")), rs.getString("support_style"),
                rs.getBoolean("stronger_content_controls"), rs.getBoolean("private_feed"),
                rs.getBoolean("blur_sensitive_content"), rs.getBoolean("reduce_medication_content"),
                rs.getBoolean("allow_message_requests"), rs.getBoolean("professionals_only_messages"),
                strings(rs.getArray("muted_terms"))), userId).stream().findFirst();
    }

    public void update(UUID userId, UpdateUserPreferencesRequest request) {
        UserPreferencesResponse current = find(userId).orElseGet(() -> {
            jdbcTemplate.update("INSERT INTO user_preferences (user_id) VALUES (?) ON CONFLICT DO NOTHING", userId);
            return find(userId).orElseThrow();
        });
        jdbcTemplate.update("""
                UPDATE user_preferences
                   SET support_topics = ?, support_style = ?, stronger_content_controls = ?, private_feed = ?,
                       blur_sensitive_content = ?, reduce_medication_content = ?, allow_message_requests = ?,
                       professionals_only_messages = ?, muted_terms = ?
                 WHERE user_id = ?
                """,
                request.supportTopics() == null ? current.supportTopics().toArray(String[]::new) : clean(request.supportTopics()),
                request.supportStyle() == null ? current.supportStyle() : normalize(request.supportStyle()),
                value(request.strongerContentControls(), current.strongerContentControls()),
                value(request.privateFeed(), current.privateFeed()),
                value(request.blurSensitiveContent(), current.blurSensitiveContent()),
                value(request.reduceMedicationContent(), current.reduceMedicationContent()),
                value(request.allowMessageRequests(), current.allowMessageRequests()),
                value(request.professionalsOnlyMessages(), current.professionalsOnlyMessages()),
                request.mutedTerms() == null ? current.mutedTerms().toArray(String[]::new) : clean(request.mutedTerms()),
                userId);
    }

    private boolean value(Boolean requested, boolean current) { return requested == null ? current : requested; }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String[] clean(List<String> values) {
        return values.stream().map(String::trim).filter(value -> !value.isBlank()).distinct().toArray(String[]::new);
    }
    private List<String> strings(Array array) throws SQLException {
        return array == null ? List.of() : Arrays.asList((String[]) array.getArray());
    }
}
