package com.greenocean.backend.account.dto;

import com.greenocean.backend.moderation.dto.ReportResponse;
import com.greenocean.backend.preference.dto.UserPreferencesResponse;
import com.greenocean.backend.profile.dto.OwnProfileResponse;
import com.greenocean.backend.social.dto.SocialConnectionResponse;
import com.greenocean.backend.wellbeing.dto.WellbeingSnapshotResponse;
import com.greenocean.backend.search.dto.SearchHistoryResponse;

import java.time.Instant;
import java.util.List;

public record AccountArchiveResponse(
        Instant generatedAt,
        OwnProfileResponse profile,
        UserPreferencesResponse preferences,
        WellbeingSnapshotResponse wellbeing,
        List<ReportResponse> reports,
        List<SocialConnectionResponse> following,
        List<SocialConnectionResponse> followers,
        List<SocialConnectionResponse> blockedAccounts,
        List<SearchHistoryResponse> searchHistory,
        AccountActivityResponse activity,
        java.util.Map<String,Object> governance,
        java.util.List<java.util.Map<String,Object>> socialDrafts,
        java.util.List<java.util.Map<String,Object>> socialModeration,
        java.util.List<java.util.Map<String,Object>> messageRequests,
        java.util.Map<String,Object> groupGovernance,
        java.util.Map<String,Object> listenerPlanning,
        java.util.Map<String,Object> privateWellbeing,
        java.util.Map<String,Object> professionalCare
) {
}
