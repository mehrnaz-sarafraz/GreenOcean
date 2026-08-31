package com.greenocean.backend.catalog.dto;

import java.util.List;
import java.util.UUID;

public record MediaRecommendationResponse(
        UUID id, String title, String kind, short year, String duration, String theme, String description,
        String discussionPrompt, List<String> contentNotes, String recommendedBy, String accent,
        String softAccent, boolean saved
) {
}
