package com.example.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Global exception handler — catches anything that isn't handled by a
 * controller's own try-catch and translates it into a clean HTTP response.
 *
 * Logging rules enforced here:
 *   - Never log the Authorization header (contains the JWT bearer token).
 *   - Never log raw query strings — they can carry tokens on some flows.
 *   - Never log passwords or user credentials.
 *   - Use parameterised SLF4J calls (log.warn("msg {}", val)) so there's
 *     no string concatenation that could accidentally inline sensitive data.
 *   - Stack traces are logged only for genuinely unexpected 500s (ERROR
 *     level). Known business errors (4xx) are logged at WARN without traces.
 *   - The response body never includes internal details; only the sanitised
 *     message the service layer already chose to surface.
 *
 * ResponseStatusException is handled explicitly so the intended status code
 * is preserved — our catch-all Exception handler would otherwise intercept it
 * and return 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * ResponseStatusException — thrown by controllers when they explicitly want
     * to return a specific HTTP status (e.g. 403 on ownership denial, 404 on
     * missing resource). Spring's own resolver would normally handle these, but
     * our catch-all Exception handler intercepts them first, so we handle them
     * here explicitly to preserve the intended status code and reason message.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(
            ResponseStatusException ex, HttpServletRequest request) {
        int status = ex.getStatusCode().value();
        String reason = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        log.debug("Request rejected at [{}]: status={}, reason={}", sanitisePath(request), status, reason);
        return ResponseEntity.status(status).body(ErrorResponse.of(status, reason));
    }

    /**
     * Validation failure — service rejected the request (e.g. negative price).
     * HTTP 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Validation failure at [{}]: {}", sanitisePath(request), ex.getMessage());
        return ResponseEntity.badRequest().body(ErrorResponse.of(400, ex.getMessage()));
    }

    /**
     * Business rule / state violation (e.g. insufficient stock).
     * HTTP 409 Conflict.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request) {
        log.warn("Business rule violation at [{}]: {}", sanitisePath(request), ex.getMessage());
        return ResponseEntity.status(409).body(ErrorResponse.of(409, ex.getMessage()));
    }

    /**
     * Ownership / access violation thrown by the service layer.
     * HTTP 403 Forbidden.
     *
     * The exception message is logged server-side for audit purposes but is
     * NOT echoed back to the client — we return a generic "Access denied" to
     * avoid leaking information about which resource exists or who owns it.
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurity(
            SecurityException ex, HttpServletRequest request) {
        log.warn("Access denied at [{}]: {}", sanitisePath(request), ex.getMessage());
        return ResponseEntity.status(403).body(ErrorResponse.of(403, "Access denied"));
    }

    /**
     * Catch-all for anything unexpected.
     * HTTP 500 Internal Server Error.
     *
     * Logged at ERROR with the full stack trace so we can diagnose it.
     * The response body is deliberately vague — internal error messages
     * often contain class names, SQL, or paths that shouldn't reach clients.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        // Log full exception at ERROR — this is a genuine unexpected failure
        log.error("Unhandled exception at [{}]", sanitisePath(request), ex);
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.of(500, "An internal error occurred"));
    }

    /**
     * Returns just the request path, stripping the query string.
     * Query strings can contain tokens (e.g. ?token=...) on some flows,
     * so we never let them reach the log.
     */
    private String sanitisePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) return "unknown";
        int q = uri.indexOf('?');
        return q >= 0 ? uri.substring(0, q) : uri;
    }
}