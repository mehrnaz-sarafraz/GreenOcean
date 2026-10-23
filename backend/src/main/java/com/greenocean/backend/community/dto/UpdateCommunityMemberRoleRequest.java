package com.greenocean.backend.community.dto;

import jakarta.validation.constraints.Pattern;

public record UpdateCommunityMemberRoleRequest(
        @Pattern(regexp = "MEMBER|MODERATOR") String role
) {
}
