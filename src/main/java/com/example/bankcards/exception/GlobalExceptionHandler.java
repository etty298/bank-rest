package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the Bank Cards REST API.
 * <p>
 * This class uses Spring's {@link ControllerAdvice} to provide centralized
 * exception handling across all controllers. It catches exceptions thrown
 * by controllers and converts them into appropriate HTTP responses.
 * <p>
 * The handler provides consistent error responses in the following format:
 * <pre>
 * {
 *   "error": "ERROR_CODE",
 *   "message": "Detailed error message"
 * }
 * </pre>
 * <p>
 * Handled exceptions and their HTTP status codes:
 * <ul>
 *   <li>{@link NotFoundException} → 404 Not Found</li>
 *   <li>{@link AccessDeniedException} → 403 Forbidden</li>
 *   <li>{@link MethodArgumentNotValidException} → 400 Bad Request (validation errors)</li>
 *   <li>{@link IllegalArgumentException} → 400 Bad Request</li>
 *   <li>{@link BadCredentialsException} → 401 Unauthorized</li>
 *   <li>{@link Exception} (general) → 500 Internal Server Error</li>
 * </ul>
 *
 * @see ControllerAdvice
 * @see ExceptionHandler
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link NotFoundException} and returns HTTP 404 Not Found.
     * <p>
     * This handler is invoked when a requested resource (card, user, etc.)
     * cannot be found in the system.
     *
     * @param ex the NotFoundException that was thrown
     * @return ResponseEntity with error details and 404 status code
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("NOT_FOUND", ex.getMessage()));
    }

    /**
     * Handles {@link AccessDeniedException} and returns HTTP 403 Forbidden.
     * <p>
     * This handler is invoked when a user attempts to access a resource
     * they don't have permission to access.
     *
     * @param ex the AccessDeniedException that was thrown
     * @return ResponseEntity with error details and 403 status code
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccess(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error("FORBIDDEN", ex.getMessage()));
    }

    /**
     * Handles validation errors from {@link MethodArgumentNotValidException}.
     * <p>
     * This handler is invoked when request body validation fails (e.g., missing
     * required fields, invalid formats). It returns a list of all validation errors.
     *
     * @param ex the MethodArgumentNotValidException containing validation errors
     * @return ResponseEntity with validation error details and 400 status code
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "VALIDATION_ERROR");
        body.put("message", ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).toList());
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles {@link IllegalArgumentException} and returns HTTP 400 Bad Request.
     * <p>
     * This handler is invoked when invalid arguments are passed to service methods,
     * such as invalid transfer amounts or card statuses.
     *
     * @param ex the IllegalArgumentException that was thrown
     * @return ResponseEntity with error details and 400 status code
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegal(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(error("BAD_REQUEST", ex.getMessage()));
    }

    /**
     * Handles authentication failures from {@link BadCredentialsException}.
     * <p>
     * This handler is invoked when user authentication fails due to incorrect
     * username or password during login.
     *
     * @param ex the BadCredentialsException that was thrown
     * @return ResponseEntity with error details and 401 status code
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("UNAUTHORIZED", "Неверные учётные данные"));
    }

    /**
     * Handles all other unhandled exceptions.
     * <p>
     * This is a catch-all handler for any exceptions not explicitly handled
     * by other methods. It returns HTTP 500 Internal Server Error.
     *
     * @param ex the Exception that was thrown
     * @return ResponseEntity with error details and 500 status code
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("INTERNAL_ERROR", ex.getMessage()));
    }

    /**
     * Creates a standardized error response map.
     * <p>
     * Helper method to create consistent error response structure
     * with an error code and message.
     *
     * @param code the error code (e.g., "NOT_FOUND", "FORBIDDEN")
     * @param message the detailed error message
     * @return a map containing the error code and message
     */
    private Map<String, Object> error(String code, String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("error", code);
        map.put("message", message);
        return map;
    }
}



