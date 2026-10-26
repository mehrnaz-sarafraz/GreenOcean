package com.greenocean.backend.common.exception;
public class TooManyRequestsException extends RuntimeException {
    private final long retryAfter;
    public TooManyRequestsException(String message,long retryAfter){super(message);this.retryAfter=retryAfter;}
    public long retryAfter(){return retryAfter;}
}
