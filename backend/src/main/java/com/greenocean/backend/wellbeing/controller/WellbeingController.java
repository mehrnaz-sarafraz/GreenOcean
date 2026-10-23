package com.greenocean.backend.wellbeing.controller;

import com.greenocean.backend.wellbeing.dto.*;
import com.greenocean.backend.wellbeing.service.WellbeingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wellbeing")
public class WellbeingController {
    private final WellbeingService service;
    public WellbeingController(WellbeingService service) { this.service = service; }

    @GetMapping
    public WellbeingSnapshotResponse snapshot(@AuthenticationPrincipal Jwt jwt) { return service.snapshot(userId(jwt)); }

    @PostMapping("/check-ins")
    @ResponseStatus(HttpStatus.CREATED)
    public WellbeingSnapshotResponse.CheckIn checkIn(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCheckInRequest request) { return service.checkIn(userId(jwt), request); }

    @PostMapping("/reflections")
    @ResponseStatus(HttpStatus.CREATED)
    public WellbeingSnapshotResponse.Reflection reflection(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateReflectionRequest request) { return service.reflection(userId(jwt), request); }

    @PostMapping("/journal")
    @ResponseStatus(HttpStatus.CREATED)
    public WellbeingSnapshotResponse.JournalEntry journal(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateJournalEntryRequest request) { return service.journal(userId(jwt), request); }

    @DeleteMapping("/journal/{id}")
    public ResponseEntity<Void> deleteJournal(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.deleteJournal(id, userId(jwt)); return ResponseEntity.noContent().build();
    }

    @PostMapping("/goals")
    @ResponseStatus(HttpStatus.CREATED)
    public WellbeingSnapshotResponse.Goal goal(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateGoalRequest request) { return service.goal(userId(jwt), request); }

    @PatchMapping("/goals/{id}")
    public WellbeingSnapshotResponse.Goal updateGoal(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
            @Valid @RequestBody UpdateGoalRequest request) { return service.updateGoal(id, userId(jwt), request); }

    @PatchMapping("/growth-paths/{id}")
    public WellbeingSnapshotResponse.GrowthPath updateGrowthPath(@AuthenticationPrincipal Jwt jwt,
                                                                  @PathVariable UUID id,
                                                                  @Valid @RequestBody UpdateGrowthPathProgressRequest request) {
        return service.updateGrowthPath(id, userId(jwt), request);
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
