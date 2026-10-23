package com.greenocean.backend.catalog.dto;

import java.util.UUID;

public record CatalogOptionResponse(
        UUID id,
        String group,
        String key,
        String label,
        String description,
        String icon,
        String accent,
        String softAccent,
        short sortOrder
) {
}
