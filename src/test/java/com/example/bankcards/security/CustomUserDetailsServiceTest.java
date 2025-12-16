package com.example.bankcards.security;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("CustomUserDetailsService - Comprehensive Tests")
class CustomUserDetailsServiceTest {

    private UserRepository userRepository;
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        userDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Test
    @DisplayName("loadUserByUsername() should return UserDetails for existing enabled user")
    void loadUserByUsernameShouldReturnUserDetailsForEnabledUser() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded_password")
                .role(Role.USER)
                .enabled(true)
                .build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encoded_password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("loadUserByUsername() should return disabled UserDetails for disabled user")
    void loadUserByUsernameShouldReturnDisabledUserDetails() {
        // Given
        User user = User.builder()
                .id(2L)
                .username("disableduser")
                .password("encoded_password")
                .role(Role.USER)
                .enabled(false)
                .build();
        when(userRepository.findByUsername("disableduser")).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("disableduser");

        // Then
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
    }

    @Test
    @DisplayName("loadUserByUsername() should set ROLE_ADMIN authority for admin user")
    void loadUserByUsernameShouldSetAdminRole() {
        // Given
        User user = User.builder()
                .id(3L)
                .username("admin")
                .password("encoded_password")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        // Then
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertEquals(1, userDetails.getAuthorities().size());
    }

    @Test
    @DisplayName("loadUserByUsername() should throw UsernameNotFoundException when user not found")
    void loadUserByUsernameShouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("nonexistent"));
        assertEquals("User not found: nonexistent", exception.getMessage());
    }

    @Test
    @DisplayName("loadUserByUsername() should call repository findByUsername")
    void loadUserByUsernameShouldCallRepository() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .role(Role.USER)
                .enabled(true)
                .build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        userDetailsService.loadUserByUsername("testuser");

        // Then
        verify(userRepository).findByUsername("testuser");
    }
}
