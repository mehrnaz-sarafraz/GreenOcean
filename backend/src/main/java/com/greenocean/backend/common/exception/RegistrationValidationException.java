package com.greenocean.backend.common.exception;

/** Carries a safe correctable field error, never a rejected value. */
public class RegistrationValidationException extends IllegalArgumentException {
    private final String field;
    public RegistrationValidationException(String field, String message) {
        super(message);
        this.field = field;
    }
    public String field() { return field; }
}
