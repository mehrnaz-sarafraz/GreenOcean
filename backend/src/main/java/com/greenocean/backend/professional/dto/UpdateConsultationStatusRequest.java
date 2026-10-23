package com.greenocean.backend.professional.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateConsultationStatusRequest(@NotBlank String status) {
}
