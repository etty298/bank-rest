package com.example.bankcards.entity;

/**
 * Enumeration representing user roles in the banking system.
 * <p>
 * This enum defines the different access levels available in the application:
 * <ul>
 *   <li>{@link #ADMIN} - Full administrative access including user and card management</li>
 *   <li>{@link #USER} - Standard user access for personal card operations</li>
 * </ul>
 * <p>
 * Roles are used by Spring Security for authorization and access control.
 *
 * @see User
 * @see org.springframework.security.access.prepost.PreAuthorize
 */
public enum Role {
    /**
     * Administrator role with full system access.
     * <p>
     * Administrators can:
     * <ul>
     *   <li>Create, update, and delete users</li>
     *   <li>Create cards for any user</li>
     *   <li>Activate, block, and delete cards</li>
     *   <li>View all cards in the system</li>
     * </ul>
     */
    ADMIN,
    
    /**
     * Standard user role with limited access.
     * <p>
     * Regular users can:
     * <ul>
     *   <li>View their own cards</li>
     *   <li>Check card balances</li>
     *   <li>Transfer money between their own cards</li>
     * </ul>
     */
    USER
}



