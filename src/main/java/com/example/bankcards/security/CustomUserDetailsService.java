package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService}.
 * <p>
 * This service is responsible for loading user-specific data during authentication.
 * It retrieves user information from the database and converts it into a format
 * that Spring Security can use for authentication and authorization.
 * <p>
 * The service:
 * <ul>
 *   <li>Loads users from the database by username</li>
 *   <li>Converts user roles to Spring Security authorities (prefixed with "ROLE_")</li>
 *   <li>Handles enabled/disabled user status</li>
 *   <li>Throws UsernameNotFoundException if user is not found</li>
 * </ul>
 * <p>
 * This implementation is used by:
 * <ul>
 *   <li>Spring Security's authentication manager during login</li>
 *   <li>JWT authentication filter to load user details from token</li>
 * </ul>
 *
 * @see UserDetailsService
 * @see JwtAuthenticationFilter
 * @see com.example.bankcards.service.AuthService
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    /**
     * Loads a user by their username for Spring Security authentication.
     * <p>
     * This method is called by Spring Security during authentication to retrieve
     * user details. It converts the application's User entity into Spring Security's
     * UserDetails object.
     * <p>
     * The user's role is converted to a Spring Security authority by prefixing
     * it with "ROLE_" (e.g., "ADMIN" becomes "ROLE_ADMIN").
     *
     * @param username the username of the user to load
     * @return UserDetails object containing user authentication information
     * @throws UsernameNotFoundException if no user is found with the given username
     * @see UserDetails
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }
}



