package com.example.bankcards.service;

import com.example.bankcards.dto.user.CreateUserRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("UserService - Comprehensive Tests")
class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("create() should encode password and save user with enabled=true")
    void createShouldEncodePasswordAndSaveUserAsEnabled() {
        // Given
        CreateUserRequest request = new CreateUserRequest("testuser", "password123", Role.USER);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        User savedUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded_password")
                .role(Role.USER)
                .enabled(true)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserResponse response = userService.create(request);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals("testuser", capturedUser.getUsername());
        assertEquals("encoded_password", capturedUser.getPassword());
        assertEquals(Role.USER, capturedUser.getRole());
        assertTrue(capturedUser.isEnabled());

        assertEquals(1L, response.id());
        assertEquals("testuser", response.username());
        assertEquals(Role.USER, response.role());
        assertTrue(response.enabled());
    }

    @Test
    @DisplayName("create() should support ADMIN role")
    void createShouldSupportAdminRole() {
        // Given
        CreateUserRequest request = new CreateUserRequest("admin", "adminpass", Role.ADMIN);
        when(passwordEncoder.encode("adminpass")).thenReturn("encoded_admin");
        User savedUser = User.builder()
                .id(2L)
                .username("admin")
                .password("encoded_admin")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserResponse response = userService.create(request);

        // Then
        assertEquals(Role.ADMIN, response.role());
        verify(passwordEncoder).encode("adminpass");
    }

    @Test
    @DisplayName("findAll() should return list of all users mapped to UserResponse")
    void findAllShouldReturnAllUsersMappedToResponse() {
        // Given
        User user1 = User.builder().id(1L).username("user1").role(Role.USER).enabled(true).build();
        User user2 = User.builder().id(2L).username("user2").role(Role.ADMIN).enabled(false).build();
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // When
        List<UserResponse> responses = userService.findAll();

        // Then
        assertEquals(2, responses.size());
        assertEquals("user1", responses.get(0).username());
        assertEquals(Role.USER, responses.get(0).role());
        assertTrue(responses.get(0).enabled());

        assertEquals("user2", responses.get(1).username());
        assertEquals(Role.ADMIN, responses.get(1).role());
        assertFalse(responses.get(1).enabled());
    }

    @Test
    @DisplayName("findAll() should return empty list when no users exist")
    void findAllShouldReturnEmptyListWhenNoUsers() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());

        // When
        List<UserResponse> responses = userService.findAll();

        // Then
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("findById() should return user when found")
    void findByIdShouldReturnUserWhenFound() {
        // Given
        User user = User.builder().id(1L).username("testuser").role(Role.USER).enabled(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        UserResponse response = userService.findById(1L);

        // Then
        assertEquals(1L, response.id());
        assertEquals("testuser", response.username());
    }

    @Test
    @DisplayName("findById() should throw NotFoundException when user not found")
    void findByIdShouldThrowNotFoundExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, 
            () -> userService.findById(99L));
        assertEquals("User not found: 99", exception.getMessage());
    }

    @Test
    @DisplayName("getById() should return User entity when found")
    void getByIdShouldReturnUserEntityWhenFound() {
        // Given
        User user = User.builder().id(1L).username("testuser").role(Role.USER).enabled(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        User result = userService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("getById() should throw NotFoundException when user not found")
    void getByIdShouldThrowNotFoundException() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> userService.getById(99L));
        assertEquals("User not found: 99", exception.getMessage());
    }

    @Test
    @DisplayName("delete() should call repository deleteById")
    void deleteShouldCallRepositoryDeleteById() {
        // When
        userService.delete(1L);

        // Then
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("UserResponse mapping should preserve all user fields")
    void userResponseMappingShouldPreserveAllFields() {
        // Given
        User disabledUser = User.builder()
                .id(5L)
                .username("disabled_user")
                .role(Role.USER)
                .enabled(false)
                .build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(disabledUser));

        // When
        UserResponse response = userService.findById(5L);

        // Then
        assertEquals(5L, response.id());
        assertEquals("disabled_user", response.username());
        assertEquals(Role.USER, response.role());
        assertFalse(response.enabled());
    }
}
