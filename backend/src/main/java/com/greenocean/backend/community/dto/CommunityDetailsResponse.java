package com.greenocean.backend.community.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CommunityDetailsResponse(
        List<Rule> rules,
        List<Member> members,
        List<Event> events
) {
    public record Rule(UUID id, int position, String text) {}

    public record Member(
            UUID userId,
            String displayName,
            String username,
            String avatarUrl,
            String role,
            Instant joinedAt
    ) {}

    public record Event(
            UUID id,
            String title,
            String description,
            String format,
            String hostName,
            Instant startsAt,
            Instant endsAt,
            Integer capacity,
            long attendeeCount,
            boolean joined,
            String status
    ) {}
}
