package com.greenocean.backend.wellbeing.service;

import com.greenocean.backend.common.exception.NotFoundException;
import com.greenocean.backend.common.persistence.DatabaseUuidGenerator;
import com.greenocean.backend.wellbeing.dto.*;
import com.greenocean.backend.wellbeing.repository.WellbeingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class WellbeingService {
    private final WellbeingRepository repository;
    private final DatabaseUuidGenerator uuidGenerator;
    public WellbeingService(WellbeingRepository repository, DatabaseUuidGenerator uuidGenerator) {
        this.repository = repository; this.uuidGenerator = uuidGenerator;
    }

    @Transactional(readOnly = true)
    public WellbeingSnapshotResponse snapshot(UUID userId) { return repository.snapshot(userId); }

    @Transactional
    public WellbeingSnapshotResponse.CheckIn checkIn(UUID userId, CreateCheckInRequest request) {
        UUID id = uuidGenerator.nextUuid(); repository.createCheckIn(id, userId, request); return repository.checkIn(id, userId);
    }
    @Transactional
    public WellbeingSnapshotResponse.Reflection reflection(UUID userId, CreateReflectionRequest request) {
        int score = request.answers().stream().mapToInt(Integer::intValue).sum();
        String band = score >= 15 ? "SUPPORTED" : score >= 8 ? "MIXED" : "STRAINED";
        UUID id = uuidGenerator.nextUuid(); repository.createReflection(id, userId, request.answers(), score, band);
        return repository.reflection(id, userId);
    }
    @Transactional
    public WellbeingSnapshotResponse.JournalEntry journal(UUID userId, CreateJournalEntryRequest request) {
        UUID id = uuidGenerator.nextUuid(); repository.createJournal(id, userId, request); return repository.journal(id, userId);
    }
    @Transactional
    public void deleteJournal(UUID id, UUID userId) {
        if (!repository.deleteJournal(id, userId)) throw new NotFoundException("Journal entry was not found");
    }
    @Transactional
    public WellbeingSnapshotResponse.Goal goal(UUID userId, CreateGoalRequest request) {
        UUID id = uuidGenerator.nextUuid(); repository.createGoal(id, userId, request); return repository.goal(id, userId);
    }
    @Transactional
    public WellbeingSnapshotResponse.Goal updateGoal(UUID id, UUID userId, UpdateGoalRequest request) {
        throw new com.greenocean.backend.common.exception.ConflictException("Use versioned goal status and actual completed steps; arbitrary progress is no longer accepted.");
    }

    @Transactional
    public WellbeingSnapshotResponse.GrowthPath updateGrowthPath(UUID id, UUID userId,
                                                                  UpdateGrowthPathProgressRequest request) {
        if (!repository.updateGrowthPath(id, userId, request.progress())) {
            throw new NotFoundException("Growth path was not found");
        }
        return repository.growthPaths(userId).stream().filter(path -> path.id().equals(id)).findFirst()
                .orElseThrow(() -> new NotFoundException("Growth path was not found"));
    }
}
