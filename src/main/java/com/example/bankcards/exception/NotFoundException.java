package com.example.bankcards.exception;

/**
 * Exception thrown when a requested resource cannot be found in the system.
 * <p>
 * This exception is typically thrown when:
 * <ul>
 *   <li>A card with a specific ID does not exist</li>
 *   <li>A user with a specific ID or username does not exist</li>
 *   <li>Any other entity requested by ID cannot be found in the database</li>
 * </ul>
 * <p>
 * This exception is handled by {@link GlobalExceptionHandler} and returns
 * an HTTP 404 Not Found response to the client.
 *
 * @see GlobalExceptionHandler#handleNotFound(NotFoundException)
 */
public class NotFoundException extends RuntimeException {
    /**
     * Constructs a new NotFoundException with the specified detail message.
     *
     * @param message the detail message explaining what resource was not found
     */
    public NotFoundException(String message) { super(message); }
}



