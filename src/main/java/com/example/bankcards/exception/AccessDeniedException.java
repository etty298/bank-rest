package com.example.bankcards.exception;

/**
 * Exception thrown when a user attempts to access a resource they don't have permission to access.
 * <p>
 * This exception is typically thrown when:
 * <ul>
 *   <li>A user tries to access another user's cards or data</li>
 *   <li>A non-admin user attempts to perform administrative operations</li>
 *   <li>Business rules prevent access to a specific resource</li>
 * </ul>
 * <p>
 * This exception is handled by {@link GlobalExceptionHandler} and returns
 * an HTTP 403 Forbidden response to the client.
 *
 * @see GlobalExceptionHandler#handleAccess(AccessDeniedException)
 */
public class AccessDeniedException extends RuntimeException {
    /**
     * Constructs a new AccessDeniedException with the specified detail message.
     *
     * @param message the detail message explaining why access was denied
     */
    public AccessDeniedException(String message) { super(message); }
}



