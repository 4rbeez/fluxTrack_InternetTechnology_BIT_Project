package com.example.demo.controller;

import java.time.LocalDateTime;

/**
 * Uniform JSON shape returned for all error responses.
 *
 * Keeping this minimal on purpose: status + message is all the client
 * needs to display a meaningful error. Internal details (stack traces,
 * SQL errors, class names) are intentionally excluded here — those go
 * to the server log only, never to the HTTP response body.
 */
public record ErrorResponse(
    int status,
    String message,
    String timestamp
) {
    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, LocalDateTime.now().toString());
    }
}
