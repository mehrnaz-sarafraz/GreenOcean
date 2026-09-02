package com.greenocean.backend.messaging.dto;

public record SupportAvailabilityResponse(
        long availableListeners,
        long peersOnline,
        int estimatedWaitSeconds
) {
}
