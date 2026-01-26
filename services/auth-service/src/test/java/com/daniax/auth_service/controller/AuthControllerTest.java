package com.daniax.auth_service.controller;

import com.daniax.auth_service.AuthException.GlobalException;
import com.daniax.auth_service.dto.*;
import com.daniax.auth_service.entity.Role;
import com.daniax.auth_service.entity.User;
import com.daniax.auth_service.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private LoginUserDto loginUserDto;
    private AuthResponse authResponse;
    private ChangeMdpDto changeMdpDto;
    private RegisterUserDto registerUserDto;
    private User user;

    @BeforeEach
    void setUp() {
        loginUserDto = new LoginUserDto();
        loginUserDto.setUserName("testuser");
        loginUserDto.setMdp("password");

        authResponse = new AuthResponse("token", "testuser", "USER", true);

        changeMdpDto = new ChangeMdpDto();
        changeMdpDto.setUsername("testuser");
        changeMdpDto.setOldPassword("oldPassword");
        changeMdpDto.setNewPassword("newPassword");

        registerUserDto = new RegisterUserDto();
        registerUserDto.setUserName("newuser");
        registerUserDto.setEmail("new@example.com");
        registerUserDto.setMdp("password");
        registerUserDto.setRole(1);

        user = new User();
        user.setId(1L);
        user.setUserName("newuser");
        user.setEmail("new@example.com");
        user.setRole(Role.USER);
    }

    @Test
    void testLogin_Success() throws GlobalException {
        // Arrange
        when(authService.login(loginUserDto)).thenReturn(authResponse);

        // Act
        ResponseEntity<?> response = authController.login(loginUserDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(authResponse, response.getBody());
        verify(authService).login(loginUserDto);
    }

    @Test
    void testLogin_Failure() throws GlobalException {
        // Arrange
        when(authService.login(loginUserDto)).thenThrow(new GlobalException("Invalid credentials"));

        // Act
        ResponseEntity<?> response = authController.login(loginUserDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody());
        verify(authService).login(loginUserDto);
    }

    @Test
    void testChange_Success() throws GlobalException {
        // Arrange
        doNothing().when(authService).changePassword(changeMdpDto);

        // Act
        ResponseEntity<?> response = authController.change(changeMdpDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Mdp changed", response.getBody());
        verify(authService).changePassword(changeMdpDto);
    }

    @Test
    void testChange_Failure() throws GlobalException {
        // Arrange
        doThrow(new GlobalException("Old password is incorrect")).when(authService).changePassword(changeMdpDto);

        // Act
        ResponseEntity<?> response = authController.change(changeMdpDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Old password is incorrect", response.getBody());
        verify(authService).changePassword(changeMdpDto);
    }

    @Test
    void testGetOtherUsers_Success() throws GlobalException {
        // Arrange
        List<UserDto> users = new ArrayList<>();
        UserDto userDto = new UserDto(2L, "user2", "user2@example.com", "USER");
        users.add(userDto);

        when(authService.otherUsers(1L)).thenReturn(users);

        // Act
        ResponseEntity<?> response = authController.getOtherUsers(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(authService).otherUsers(1L);
    }

    @Test
    void testGetOtherUsers_Failure() throws GlobalException {
        // Arrange
        when(authService.otherUsers(1L)).thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<?> response = authController.getOtherUsers(1L);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error", response.getBody());
        verify(authService).otherUsers(1L);
    }

    @Test
    void testGetUserName_Success() throws GlobalException {
        // Arrange
        when(authService.userName(1L)).thenReturn("testuser");

        // Act
        ResponseEntity<String> response = authController.getUserName(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("testuser", response.getBody());
        verify(authService).userName(1L);
    }

    @Test
    void testGetUserName_Failure() throws GlobalException {
        // Arrange
        when(authService.userName(1L)).thenThrow(new GlobalException("User not found"));

        // Act
        ResponseEntity<String> response = authController.getUserName(1L);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User not found", response.getBody());
        verify(authService).userName(1L);
    }

    @Test
    void testRegister_Success() throws GlobalException {
        // Arrange
        when(authService.create(any(User.class))).thenReturn(user);

        // Act
        ResponseEntity<?> response = authController.register(registerUserDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(authService).create(any(User.class));
    }

    @Test
    void testRegister_Failure() throws GlobalException {
        // Arrange
        when(authService.create(any(User.class))).thenThrow(new GlobalException("User already exists"));

        // Act
        ResponseEntity<?> response = authController.register(registerUserDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User already exists", response.getBody());
        verify(authService).create(any(User.class));
    }

    @Test
    void testUsers_Success() throws GlobalException {
        // Arrange
        List<UserDto> users = new ArrayList<>();
        UserDto userDto = new UserDto(1L, "user1", "user1@example.com", "USER");
        users.add(userDto);

        when(authService.users()).thenReturn(users);

        // Act
        ResponseEntity<?> response = authController.users();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(authService).users();
    }

    @Test
    void testUsers_Failure() throws GlobalException {
        // Arrange
        when(authService.users()).thenThrow(new GlobalException("Error"));

        // Act
        ResponseEntity<?> response = authController.users();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error", response.getBody());
        verify(authService).users();
    }
}
