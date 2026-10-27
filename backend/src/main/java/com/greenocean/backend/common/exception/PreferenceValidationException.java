package com.greenocean.backend.common.exception;

/** Safe, value-free preference errors; not a persistence programming exception. */
public class PreferenceValidationException extends RuntimeException {
    public PreferenceValidationException(String message) { super(message); }
}
