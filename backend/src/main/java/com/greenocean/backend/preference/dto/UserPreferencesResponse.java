package com.greenocean.backend.preference.dto;

import java.util.List;

public record UserPreferencesResponse(
        List<String> supportTopics,
        String supportStyle,
        boolean strongerContentControls,
        boolean privateFeed,
        boolean blurSensitiveContent,
        boolean reduceMedicationContent,
        boolean allowMessageRequests,
        boolean professionalsOnlyMessages,
        List<String> mutedTerms
) {
}
