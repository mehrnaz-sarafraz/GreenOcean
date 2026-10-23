package com.greenocean.backend.catalog.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CrisisResourceResponse(
        UUID id, String countryCode, String regionName, String title, String detail,
        String actionUrl, String sourceUrl, LocalDate lastVerifiedOn, short sortOrder
) {
}
