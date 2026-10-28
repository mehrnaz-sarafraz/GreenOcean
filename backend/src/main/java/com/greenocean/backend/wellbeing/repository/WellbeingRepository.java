package com.greenocean.backend.wellbeing.repository;

import com.greenocean.backend.wellbeing.dto.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class WellbeingRepository {
    private final JdbcTemplate jdbcTemplate;
    public WellbeingRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public WellbeingSnapshotResponse snapshot(UUID userId) {
        List<WellbeingSnapshotResponse.CheckIn> checkIns = jdbcTemplate.query("""
                SELECT id, mood, note, created_at FROM wellbeing_check_ins
                 WHERE user_id = ? ORDER BY created_at DESC LIMIT 30
                """, (rs, row) -> new WellbeingSnapshotResponse.CheckIn(rs.getObject("id", UUID.class),
                rs.getString("mood"), rs.getString("note"), rs.getObject("created_at", OffsetDateTime.class).toInstant()), userId);
        List<WellbeingSnapshotResponse.JournalEntry> journal = jdbcTemplate.query("""
                SELECT id, title, body, mood, created_at, updated_at FROM wellbeing_journal_entries
                 WHERE user_id = ? ORDER BY created_at DESC LIMIT 100
                """, (rs, row) -> new WellbeingSnapshotResponse.JournalEntry(rs.getObject("id", UUID.class),
                rs.getString("title"), rs.getString("body"), rs.getString("mood"),
                rs.getObject("created_at", OffsetDateTime.class).toInstant(),
                rs.getObject("updated_at", OffsetDateTime.class).toInstant()), userId);
        List<WellbeingSnapshotResponse.Goal> goals = jdbcTemplate.query("""
                SELECT id, title, cadence, progress, completed, created_at FROM wellbeing_goals
                 WHERE user_id = ? ORDER BY completed, created_at DESC
                """, (rs, row) -> new WellbeingSnapshotResponse.Goal(rs.getObject("id", UUID.class),
                rs.getString("title"), rs.getString("cadence"), rs.getInt("progress"),
                rs.getBoolean("completed"), rs.getObject("created_at", OffsetDateTime.class).toInstant()), userId);
        List<WellbeingSnapshotResponse.Reflection> reflections = jdbcTemplate.query("""
                SELECT id, answers, score, summary_band, created_at FROM wellbeing_reflections
                 WHERE user_id = ? ORDER BY created_at DESC LIMIT 20
                """, (rs, row) -> {
            Short[] raw = (Short[]) rs.getArray("answers").getArray();
            List<Integer> answers = Arrays.stream(raw).map(Short::intValue).toList();
            return new WellbeingSnapshotResponse.Reflection(rs.getObject("id", UUID.class), answers,
                    rs.getInt("score"), rs.getString("summary_band"),
                    rs.getObject("created_at", OffsetDateTime.class).toInstant());
        }, userId);
        return new WellbeingSnapshotResponse(checkIns, journal, goals, growthPaths(userId), reflections);
    }

    @org.springframework.beans.factory.annotation.Value("${greenocean.education.learning-enabled:false}") private boolean learningEnabled;
    public List<WellbeingSnapshotResponse.GrowthPath> growthPaths(UUID userId) {
        if(!learningEnabled)return List.of();
        return jdbcTemplate.query("""
                SELECT path.id, path.slug, path.title, path.summary, path.lesson_count, path.duration_minutes,
                       path.author_name, path.evidence_note, path.reference_list, path.reviewed_at,
                       learning_progress(?,path.resource_id,(SELECT published_revision FROM growth_resources WHERE id=path.resource_id)) AS progress,
                       learning_progress(?,path.resource_id,(SELECT published_revision FROM growth_resources WHERE id=path.resource_id))=100 AS completed,
                       progress.updated_at
                  FROM guided_growth_paths path
                  LEFT JOIN user_growth_path_progress progress
                    ON progress.path_id = path.id AND progress.user_id = ?
                 WHERE path.is_published = TRUE AND growth_resource_available(path.resource_id)
                 ORDER BY path.created_at, path.id
                """, (rs, row) -> {
            String[] references = (String[]) rs.getArray("reference_list").getArray();
            OffsetDateTime updatedAt = rs.getObject("updated_at", OffsetDateTime.class);
            return new WellbeingSnapshotResponse.GrowthPath(
                    rs.getObject("id", UUID.class), rs.getString("slug"), rs.getString("title"),
                    rs.getString("summary"), rs.getInt("lesson_count"), rs.getInt("duration_minutes"),
                    rs.getString("author_name"), rs.getString("evidence_note"), Arrays.asList(references),
                    rs.getObject("reviewed_at", java.time.LocalDate.class).toString(), rs.getInt("progress"),
                    rs.getBoolean("completed"), updatedAt == null ? null : updatedAt.toInstant());
        }, userId,userId,userId);
    }

    public boolean updateGrowthPath(UUID pathId, UUID userId, int progress) {
        return jdbcTemplate.update("""
                INSERT INTO user_growth_path_progress (user_id, path_id, progress, completed)
                SELECT ?, id, ?, ? FROM guided_growth_paths WHERE id = ? AND is_published = TRUE
                ON CONFLICT (user_id, path_id) DO UPDATE
                    SET progress = EXCLUDED.progress,
                        completed = EXCLUDED.completed,
                        updated_at = CURRENT_TIMESTAMP
                """, userId, progress, progress == 100, pathId) == 1;
    }

    public void createCheckIn(UUID id, UUID userId, CreateCheckInRequest request) {
        jdbcTemplate.update("INSERT INTO wellbeing_check_ins (id, user_id, mood, note) VALUES (?, ?, ?, ?)",
                id, userId, request.mood().trim(), normalize(request.note()));
    }

    public void createReflection(UUID id, UUID userId, List<Integer> answers, int score, String summaryBand) {
        jdbcTemplate.update("""
                INSERT INTO wellbeing_reflections (id, user_id, answers, score, summary_band)
                VALUES (?, ?, ?, ?, ?)
                """, id, userId, answers.stream().map(Integer::shortValue).toArray(Short[]::new), score, summaryBand);
    }

    public WellbeingSnapshotResponse.Reflection reflection(UUID id, UUID userId) {
        return snapshot(userId).reflections().stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow();
    }

    public void createJournal(UUID id, UUID userId, CreateJournalEntryRequest request) {
        jdbcTemplate.update("""
                INSERT INTO wellbeing_journal_entries (id, user_id, title, body, mood) VALUES (?, ?, ?, ?, ?)
                """, id, userId, request.title().trim(), request.body().trim(), normalize(request.mood()));
    }

    public boolean deleteJournal(UUID id, UUID userId) {
        return jdbcTemplate.update("DELETE FROM wellbeing_journal_entries WHERE id = ? AND user_id = ?", id, userId) == 1;
    }

    public void createGoal(UUID id, UUID userId, CreateGoalRequest request) {
        jdbcTemplate.update("INSERT INTO wellbeing_goals (id, user_id, title, cadence) VALUES (?, ?, ?, ?)",
                id, userId, request.title().trim(), request.cadence().trim());
    }

    public boolean updateGoal(UUID id, UUID userId, UpdateGoalRequest request) {
        Optional<WellbeingSnapshotResponse.Goal> current = snapshot(userId).goals().stream()
                .filter(goal -> goal.id().equals(id)).findFirst();
        if (current.isEmpty()) return false;
        int progress = request.progress() == null ? current.get().progress() : request.progress();
        boolean completed = request.completed() == null ? current.get().completed() : request.completed();
        if (completed && request.progress() == null) progress = 100;
        return jdbcTemplate.update("""
                UPDATE wellbeing_goals SET progress = ?, completed = ? WHERE id = ? AND user_id = ?
                """, progress, completed, id, userId) == 1;
    }

    public WellbeingSnapshotResponse.CheckIn checkIn(UUID id, UUID userId) {
        return snapshot(userId).checkIns().stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow();
    }
    public WellbeingSnapshotResponse.JournalEntry journal(UUID id, UUID userId) {
        return snapshot(userId).journalEntries().stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow();
    }
    public WellbeingSnapshotResponse.Goal goal(UUID id, UUID userId) {
        return snapshot(userId).goals().stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow();
    }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
