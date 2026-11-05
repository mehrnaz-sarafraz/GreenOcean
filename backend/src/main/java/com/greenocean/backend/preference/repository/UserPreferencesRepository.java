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
    @org.springframework.beans.factory.annotation.Value("${app.privacy.support-topics-enabled:false}")
    private boolean topicCollectionEnabled;
    @org.springframework.beans.factory.annotation.Value("${app.governance.optional-processing-approved:false}") private boolean optionalApproved;

    public UserPreferencesRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public Optional<UserPreferencesResponse> find(UUID userId) {
        return jdbcTemplate.query("""
                SELECT * FROM user_preferences WHERE user_id = ?
                """, (rs, row) -> new UserPreferencesResponse(
                strings(rs.getArray("support_topics")), rs.getString("support_style"),
                rs.getBoolean("stronger_content_controls"), rs.getBoolean("private_feed"),
                rs.getBoolean("blur_sensitive_content"), rs.getBoolean("reduce_medication_content"),
                rs.getBoolean("allow_message_requests"), rs.getBoolean("professionals_only_messages"),
                strings(rs.getArray("muted_terms")), rs.getBoolean("notify_messages"),
                rs.getBoolean("notify_replies"), rs.getBoolean("notify_follows"),
                rs.getBoolean("notify_groups"), rs.getBoolean("notify_articles"),
                rs.getBoolean("quiet_hours_enabled"), rs.getBoolean("onboarding_complete"),
                rs.getString("intended_role"), rs.getString("onboarding_status"), rs.getInt("onboarding_step"),
                strings(rs.getArray("legacy_support_topics")), canCollectTopics(userId)), userId).stream().findFirst();
    }

    public void update(UUID userId, UpdateUserPreferencesRequest request) {
        jdbcTemplate.update("INSERT INTO user_preferences (user_id) VALUES (?) ON CONFLICT DO NOTHING", userId);
        jdbcTemplate.queryForList("SELECT user_id FROM user_preferences WHERE user_id=? FOR UPDATE", userId);
        UserPreferencesResponse current = find(userId).orElseGet(() -> {
            jdbcTemplate.update("INSERT INTO user_preferences (user_id) VALUES (?) ON CONFLICT DO NOTHING", userId);
            return find(userId).orElseThrow();
        });
        if (request.supportTopics() != null && !request.supportTopics().isEmpty()) {
            if (!current.topicCollectionEnabled()) throw new com.greenocean.backend.common.exception.PreferenceValidationException("Topic collection is currently unavailable; you can continue without selecting topics");
            for (String topic : request.supportTopics()) {
                if (topic == null || !Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                    "SELECT EXISTS(SELECT 1 FROM support_categories WHERE slug=? AND is_active)", Boolean.class, topic)))
                    throw new com.greenocean.backend.common.exception.PreferenceValidationException("Unknown support topic identifier");
            }
        }
        if (request.supportStyle() != null && !request.supportStyle().isEmpty() &&
            (!java.util.Set.of("SHARED_EXPERIENCES","SUPPORT_GROUPS","LISTENER","PROFESSIONAL_KNOWLEDGE").contains(request.supportStyle()) ||
             !Boolean.TRUE.equals(jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM catalog_options WHERE option_group='SUPPORT_STYLE' AND option_key=? AND is_active)", Boolean.class, request.supportStyle()))))
            throw new com.greenocean.backend.common.exception.PreferenceValidationException("Unknown support style");
        String onboardingStatus = request.onboardingStatus() != null ? request.onboardingStatus() : current.onboardingStatus();
        // A delayed initial screen-mount intent must not undo an explicit dismissal.
        if ("IN_PROGRESS".equals(request.onboardingStatus()) && request.onboardingStep()==null &&
                java.util.Set.of("SKIPPED","COMPLETED","LEGACY_UNKNOWN").contains(current.onboardingStatus()))
            onboardingStatus=current.onboardingStatus();
        int step = request.onboardingStep() == null ? current.onboardingStep() : request.onboardingStep();
        if (request.onboardingStatus() == null && request.onboardingComplete() != null && request.onboardingComplete())
            onboardingStatus = "LEGACY_UNKNOWN";
        if ("COMPLETED".equals(onboardingStatus) && step != 3)
            throw new com.greenocean.backend.common.exception.PreferenceValidationException("Complete the final onboarding step before finishing");
        boolean dismissed = java.util.Set.of("SKIPPED","COMPLETED","LEGACY_UNKNOWN").contains(onboardingStatus);
        jdbcTemplate.update("""
                UPDATE user_preferences
                   SET support_topics = ?, support_style = ?, stronger_content_controls = ?, private_feed = ?,
                       blur_sensitive_content = ?, reduce_medication_content = ?, allow_message_requests = ?,
                       professionals_only_messages = ?, muted_terms = ?, notify_messages = ?, notify_replies = ?,
                       notify_follows = ?, notify_groups = ?, notify_articles = ?, quiet_hours_enabled = ?,
                       onboarding_complete = ?, intended_role = ?, onboarding_status = ?, onboarding_step = ?,
                       legacy_support_topics = ?
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
                value(request.notifyMessages(), current.notifyMessages()),
                value(request.notifyReplies(), current.notifyReplies()),
                value(request.notifyFollows(), current.notifyFollows()),
                value(request.notifyGroups(), current.notifyGroups()),
                value(request.notifyArticles(), current.notifyArticles()),
                value(request.quietHoursEnabled(), current.quietHoursEnabled()),
                dismissed,
                request.intendedRole() == null ? current.intendedRole() : request.intendedRole().trim().toUpperCase(),
                onboardingStatus, step,
                Boolean.TRUE.equals(request.clearLegacySupportTopics()) ? new String[0] : current.legacySupportTopics().toArray(String[]::new),
                userId);
    }

    public void updateIntendedRole(UUID userId, String intendedRole) {
        jdbcTemplate.update("UPDATE user_preferences SET intended_role = ? WHERE user_id = ?",
                intendedRole == null ? "MEMBER" : intendedRole.trim().toUpperCase(), userId);
    }

    private boolean canCollectTopics(UUID userId) {
        if(!topicCollectionEnabled||!optionalApproved)return false;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
          SELECT EXISTS(SELECT 1 FROM processing_authorizations a JOIN policy_documents p ON p.id=a.notice_id
           JOIN users u ON u.id=a.user_id JOIN account_eligibility e ON e.user_id=u.id
           WHERE a.user_id=? AND a.purpose='SUPPORT_TOPICS' AND a.decision='GRANTED'
           AND u.status='ACTIVE' AND u.email_verified AND e.age_result='ADULT'
           AND a.event_order=(SELECT MAX(event_order) FROM processing_authorizations WHERE user_id=? AND purpose='SUPPORT_TOPICS')
           AND p.status='PUBLISHED' AND p.review_status='APPROVED' AND p.policy_key='support-topics-processing'
           AND p.locale='en' AND p.published_at<=CURRENT_TIMESTAMP AND p.effective_at<=CURRENT_TIMESTAMP)
          """,Boolean.class,userId,userId));
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
