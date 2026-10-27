package com.greenocean.backend.preference.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
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
        @Size(max = 100) List<@Size(max = 100) String> mutedTerms,
        Boolean notifyMessages,
        Boolean notifyReplies,
        Boolean notifyFollows,
        Boolean notifyGroups,
        Boolean notifyArticles,
        Boolean quietHoursEnabled,
        Boolean onboardingComplete,
        @Pattern(regexp = "MEMBER|PROFESSIONAL") String intendedRole,
        @Pattern(regexp = "NOT_STARTED|IN_PROGRESS|SKIPPED|COMPLETED") String onboardingStatus,
        @jakarta.validation.constraints.Min(0) @jakarta.validation.constraints.Max(3) Integer onboardingStep,
        Boolean clearLegacySupportTopics
) {
}
