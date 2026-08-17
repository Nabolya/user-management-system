package com.pulse.usermanagement.exception;

import org.springframework.http.HttpStatus;

/**
 * Generic business rule violation. Carries its own HTTP status since different
 * rules map to different codes (e.g. ADMIN age -> 400, max ADMIN count / ADMIN
 * deletion -> 409).
 */
public class BusinessRuleViolationException extends RuntimeException {

    private final HttpStatus status;

    public BusinessRuleViolationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
