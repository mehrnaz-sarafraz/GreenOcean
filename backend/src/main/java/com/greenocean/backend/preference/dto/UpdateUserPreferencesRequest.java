package com.greenocean.backend.preference.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateUserPreferencesRequest(
        @Size(max = 30) List<@Size(max = 80) String> supportTopics,
        @Size(max = 40) String supportStyle,
        Boolean strongerContentControls,
        Boolean privateFeed,
        Boolean blurSensitiveContent,
        Boolean reduceMedicationContent,
        Boolean allowMessageRequests,
        Boolean professionalsOnlyMessages,
        @Size(max = 100) List<@Size(max = 100) String> mutedTerms
) {
}
