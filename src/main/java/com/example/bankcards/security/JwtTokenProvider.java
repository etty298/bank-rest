package com.example.bankcards.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * Component responsible for generating and validating JWT (JSON Web Token) tokens.
 * <p>
 * This class provides functionality to:
 * <ul>
 *   <li>Generate JWT tokens for authenticated users</li>
 *   <li>Validate JWT tokens from client requests</li>
 *   <li>Extract username and role from JWT tokens</li>
 * </ul>
 * <p>
 * The tokens are signed using HMAC-SHA256 algorithm and include:
 * <ul>
 *   <li>Subject: username</li>
 *   <li>Custom claim: user role</li>
 *   <li>Issued at timestamp</li>
 *   <li>Expiration timestamp</li>
 * </ul>
 * <p>
 * Configuration properties:
 * <ul>
 *   <li>{@code app.security.jwt.secret} - Secret key for signing tokens</li>
 *   <li>{@code app.security.jwt.expiration-minutes} - Token expiration time in minutes</li>
 * </ul>
 *
 * @see JwtAuthenticationFilter
 * @see com.example.bankcards.service.AuthService
 */
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long expirationMillis;

    /**
     * Constructs a JwtTokenProvider with the specified configuration.
     * <p>
     * Initializes the signing key and expiration time from application properties.
     *
     * @param secret the JWT secret key from configuration
     * @param expirationMinutes the token expiration time in minutes
     */
    public JwtTokenProvider(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(java.util.Base64.getEncoder().encodeToString(secret.getBytes())));
        this.expirationMillis = expirationMinutes * 60_000;
    }

    /**
     * Generates a new JWT token for a user.
     * <p>
     * The token includes the username as the subject and the role as a custom claim.
     * The token is valid for the configured expiration time.
     *
     * @param username the username to include in the token
     * @param role the user's role to include in the token
     * @return a signed JWT token string
     */
    public String generateToken(String username, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .addClaims(Map.of("role", role))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username from a JWT token.
     * <p>
     * The username is stored in the token's subject field.
     *
     * @param token the JWT token to parse
     * @return the username from the token
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     */
    public String getUsername(String token) {
        return getAllClaims(token).getSubject();
    }

    /**
     * Extracts the user role from a JWT token.
     * <p>
     * The role is stored as a custom claim in the token.
     *
     * @param token the JWT token to parse
     * @return the role from the token, or null if not present
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     */
    public String getRole(String token) {
        Object role = getAllClaims(token).get("role");
        return role == null ? null : role.toString();
    }

    /**
     * Validates a JWT token.
     * <p>
     * Checks if the token is properly signed and not expired.
     * This method catches all exceptions and returns false for invalid tokens.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validate(String token) {
        try {
            getAllClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Parses and extracts all claims from a JWT token.
     * <p>
     * This is a private helper method used by other public methods.
     *
     * @param token the JWT token to parse
     * @return the claims contained in the token
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     */
    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}



