package com.example.bankcards.service;

import com.example.bankcards.dto.auth.JwtResponse;
import com.example.bankcards.dto.auth.LoginRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling user authentication operations.
 * <p>
 * This service manages the authentication process including:
 * <ul>
 *   <li>User login with username and password</li>
 *   <li>JWT token generation for authenticated users</li>
 *   <li>Setting authentication in the security context</li>
 * </ul>
 * <p>
 * The service uses Spring Security's {@link AuthenticationManager} to verify
 * user credentials and generates JWT tokens for successful authentications.
 *
 * @see com.example.bankcards.controller.AuthController
 * @see JwtTokenProvider
 * @see org.springframework.security.authentication.AuthenticationManager
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;


    /**
     * Authenticates a user and generates a JWT token.
     * <p>
     * This method performs the following steps:
     * <ol>
     *   <li>Validates username and password using Spring Security</li>
     *   <li>Sets the authentication in the security context</li>
     *   <li>Retrieves the user from the database</li>
     *   <li>Generates a JWT token with user information</li>
     *   <li>Returns the token along with user details</li>
     * </ol>
     * <p>
     * The generated token includes the username and role as claims and has
     * a configurable expiration time.
     *
     * @param loginRequest the login credentials containing username and password
     * @return JwtResponse containing the JWT token, username, and role
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
     * @see LoginRequest
     * @see JwtResponse
     */
    @Transactional(readOnly = true)
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userRepository.findByUsername(loginRequest.username()).orElseThrow();
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());
        return new JwtResponse(token, user.getUsername(), user.getRole().name());
    }
}



