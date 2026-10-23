package com.greenocean.backend.moderation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record CreateCampaignRequest(UUID professionalId, @NotBlank @Size(max = 180) String title,
                                    @NotNull @Future Instant startsAt, @NotNull @Future Instant endsAt) {}
