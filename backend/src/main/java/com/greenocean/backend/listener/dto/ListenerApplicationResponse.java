package com.greenocean.backend.listener.dto;

import java.time.Instant;
import java.util.UUID;

public record ListenerApplicationResponse(
        UUID id, String motivation, String experience, String weeklyAvailability,
        String status, String reviewNote, Instant submittedAt, Instant reviewedAt
) {}
