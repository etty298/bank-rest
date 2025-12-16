package com.example.bankcards.controller;

import com.example.bankcards.dto.user.CreateUserRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AdminUserController - Comprehensive Tests")
class AdminUserControllerTest {

    private UserService userService;
    private AdminUserController adminUserController;

    @BeforeEach
    void setup() {
        userService = mock(UserService.class);
        adminUserController = new AdminUserController(userService);
    }

    @Test
    @DisplayName("create() should return created user with OK status")
    void createShouldReturnCreatedUserWithOkStatus() {
        // Given
        CreateUserRequest request = new CreateUserRequest("newuser", "password123", Role.USER);
        UserResponse userResponse = new UserResponse(1L, "newuser", Role.USER, true);
        when(userService.create(request)).thenReturn(userResponse);

        // When
        ResponseEntity<UserResponse> response = adminUserController.create(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("newuser", response.getBody().username());
        assertEquals(Role.USER, response.getBody().role());
        verify(userService).create(request);
    }

    @Test
    @DisplayName("findAll() should return list of all users")
    void findAllShouldReturnListOfUsers() {
        // Given
        UserResponse user1 = new UserResponse(1L, "user1", Role.USER, true);
        UserResponse user2 = new UserResponse(2L, "admin", Role.ADMIN, true);
        when(userService.findAll()).thenReturn(List.of(user1, user2));

        // When
        ResponseEntity<List<UserResponse>> response = adminUserController.findAll();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("user1", response.getBody().get(0).username());
        assertEquals("admin", response.getBody().get(1).username());
        verify(userService).findAll();
    }

    @Test
    @DisplayName("findById() should return user by id")
    void findByIdShouldReturnUser() {
        // Given
        UserResponse userResponse = new UserResponse(1L, "testuser", Role.USER, true);
        when(userService.findById(1L)).thenReturn(userResponse);

        // When
        ResponseEntity<UserResponse> response = adminUserController.findById(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1L, response.getBody().id());
        assertEquals("testuser", response.getBody().username());
        verify(userService).findById(1L);
    }

    @Test
    @DisplayName("delete() should call userService.delete and return NO_CONTENT status")
    void deleteShouldCallServiceAndReturnNoContent() {
        // Given
        doNothing().when(userService).delete(1L);

        // When
        ResponseEntity<Void> response = adminUserController.delete(1L);

        // Then
        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(userService).delete(1L);
    }

    @Test
    @DisplayName("create() should support creating admin users")
    void createShouldSupportAdminRole() {
        // Given
        CreateUserRequest request = new CreateUserRequest("adminuser", "adminpass", Role.ADMIN);
        UserResponse userResponse = new UserResponse(2L, "adminuser", Role.ADMIN, true);
        when(userService.create(request)).thenReturn(userResponse);

        // When
        ResponseEntity<UserResponse> response = adminUserController.create(request);

        // Then
        assertEquals(Role.ADMIN, response.getBody().role());
    }

    @Test
    @DisplayName("findAll() should handle empty list")
    void findAllShouldHandleEmptyList() {
        // Given
        when(userService.findAll()).thenReturn(List.of());

        // When
        ResponseEntity<List<UserResponse>> response = adminUserController.findAll();

        // Then
        assertEquals(0, response.getBody().size());
    }
}
