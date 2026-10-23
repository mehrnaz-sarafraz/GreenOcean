package com.greenocean.backend.listener.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ListenerApplicationRequest(
        @NotBlank @Size(min = 40, max = 2000) String motivation,
        @NotBlank @Size(min = 20, max = 2000) String experience,
        @NotBlank @Size(max = 80) String weeklyAvailability,
        @AssertTrue boolean acceptedCode
) {}
