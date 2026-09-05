package com.greenocean.backend.common.exception;

import com.greenocean.backend.common.api.ApiError;
import com.greenocean.backend.common.api.FieldViolation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiError> handleTooManyRequestsException(
            TooManyRequestsException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header(
                        "Retry-After",
                        Long.toString(exception.getRetryAfterSeconds())
                )
                .body(new ApiError(
                        Instant.now(),
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                        exception.getMessage(),
                        request.getRequestURI(),
                        List.of()
                ));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbiddenException(ForbiddenException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiError(
                Instant.now(), HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                exception.getMessage(), request.getRequestURI(), List.of()
        ));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(
                Instant.now(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage(), request.getRequestURI(), List.of()
        ));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorizedException(UnauthorizedException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError(
                Instant.now(), HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                exception.getMessage(), request.getRequestURI(), List.of()
        ));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflictException(ConflictException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(
                Instant.now(), HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
                exception.getMessage(), request.getRequestURI(), List.of()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException exception, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new ApiError(
                Instant.now(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                exception.getMessage(), request.getRequestURI(), List.of()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<FieldViolation> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldViolation)
                .toList();

        return ResponseEntity.badRequest().body(new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Request validation failed",
                request.getRequestURI(),
                fieldErrors
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        log.error("Unhandled API exception for {}", request.getRequestURI(), exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiError(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected server error occurred",
                request.getRequestURI(),
                List.of()
        ));
    }

    private FieldViolation toFieldViolation(FieldError fieldError) {
        return new FieldViolation(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
