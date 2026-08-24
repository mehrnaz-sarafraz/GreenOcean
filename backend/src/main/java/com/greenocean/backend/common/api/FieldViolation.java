package com.greenocean.backend.common.api;

public record FieldViolation(
        String field,
        String message
) {
}
