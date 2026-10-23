package com.greenocean.backend.listener.dto;

import java.util.List;
import java.util.UUID;

public record ListenerResponse(
        UUID id, String displayName, String username, String avatarUrl, String displayTitle,
        String listeningStyle, List<String> languages, List<String> topics,
        int completedSessions, int safetyScore, boolean online
) {}
