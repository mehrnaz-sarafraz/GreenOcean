package com.greenocean.backend.community.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateCommunityRulesRequest(
        @NotEmpty @Size(max = 20) List<@Size(min = 5, max = 500) String> rules
) {
}
