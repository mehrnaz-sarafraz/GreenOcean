package com.greenocean.backend.search.dto;

import java.time.Instant;
import java.util.UUID;

public record SearchHistoryResponse(UUID id, String term, Instant searchedAt) {
}
