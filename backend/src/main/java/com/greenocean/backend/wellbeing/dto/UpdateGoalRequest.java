package com.greenocean.backend.wellbeing.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateGoalRequest(@Min(0) @Max(100) Integer progress, Boolean completed) {}
