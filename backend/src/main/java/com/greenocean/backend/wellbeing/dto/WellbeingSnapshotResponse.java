package com.greenocean.backend.wellbeing.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WellbeingSnapshotResponse(
        List<CheckIn> checkIns,
        List<JournalEntry> journalEntries,
        List<Goal> goals,
        List<GrowthPath> growthPaths,
        List<Reflection> reflections
) {
    public record CheckIn(UUID id, String mood, String note, Instant createdAt) {}
    public record JournalEntry(UUID id, String title, String body, String mood, Instant createdAt, Instant updatedAt) {}
    public record Goal(UUID id, String title, String cadence, int progress, boolean completed, Instant createdAt) {}
    public record Reflection(UUID id, List<Integer> answers, int score, String summaryBand, Instant createdAt) {}
    public record GrowthPath(
            UUID id,
            String slug,
            String title,
            String summary,
            int lessonCount,
            int durationMinutes,
            String authorName,
            String evidenceNote,
            List<String> references,
            String reviewedAt,
            int progress,
            boolean completed,
            Instant updatedAt
    ) {}
}
