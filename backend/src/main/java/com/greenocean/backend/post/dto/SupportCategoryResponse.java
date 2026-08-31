package com.greenocean.backend.post.dto;

import java.util.UUID;

public record SupportCategoryResponse(
        UUID id,
        String slug,
        String group,
        String name,
        String description,
        String icon,
        String color,
        String softColor,
        long postCount
) {
}
