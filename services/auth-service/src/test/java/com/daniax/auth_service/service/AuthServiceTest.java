package com.daniax.auth_service.service;

import com.daniax.auth_service.client.audit.AuditAction;
import com.daniax.auth_service.client.audit.AuditClient;
import com.daniax.auth_service.client.audit.AuditRequestDto;
import com.daniax.auth_service.configuration.JwtUtils;
import com.daniax.auth_service.dto.AuthResponse;
import com.daniax.auth_service.dto.ChangeMdpDto;
import com.daniax.auth_service.dto.LoginUserDto;
import com.daniax.auth_service.dto.UserDto;
import com.daniax.auth_service.entity.Role;
import com.daniax.auth_service.entity.User;
import com.daniax.auth_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuditClient auditClient;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginUserDto loginUserDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setMdp("encodedPassword");
        testUser.setRole(Role.USER);
        testUser.setFirstLogin(true);

        loginUserDto = new LoginUserDto();
        loginUserDto.setUserName("testuser");
        loginUserDto.setMdp("password");
    }

    @Test
    void testCreateUser_Success() throws Exception {
        // Arrange
        User newUser = new User();
        newUser.setUserName("newuser");
        newUser.setEmail("new@example.com");
        newUser.setMdp("password");
        newUser.setRole(Role.USER);

        when(userRepository.findByUserName("newuser")).thenReturn(null);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        doNothing().when(auditClient).createAudit(any(AuditRequestDto.class));

        // Act
        User result = authService.create(newUser);

        // Assert
        assertNotNull(result);
        assertEquals("newuser", result.getUserName());
        verify(userRepository).findByUserName("newuser");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
        verify(auditClient).createAudit(any(AuditRequestDto.class));
    }

    @Test
    void testCreateUser_UserAlreadyExists() {
        // Arrange
        User existingUser = new User();
        existingUser.setUserName("existinguser");

        when(userRepository.findByUserName("existinguser")).thenReturn(existingUser);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.create(existingUser);
        });

        assertEquals("User already exists", exception.getMessage());
        verify(userRepository).findByUserName("existinguser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testChangePassword_Success() throws Exception {
        // Arrange
        ChangeMdpDto changeDto = new ChangeMdpDto();
        changeDto.setUsername("testuser");
        changeDto.setOldPassword("oldPassword");
        changeDto.setNewPassword("newPassword");

        when(userRepository.findByUserName("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(auditClient).createAudit(any(AuditRequestDto.class));

        // Act
        authService.changePassword(changeDto);

        // Assert
        verify(userRepository).findByUserName("testuser");
        verify(passwordEncoder).matches("oldPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(any(User.class));
        verify(auditClient, times(1)).createAudit(any(AuditRequestDto.class));
        assertFalse(Boolean.TRUE.equals(testUser.getFirstLogin()));
    }

    @Test
    void testChangePassword_UserNotFound() {
        // Arrange
        ChangeMdpDto changeDto = new ChangeMdpDto();
        changeDto.setUsername("nonexistent");

        when(userRepository.findByUserName("nonexistent")).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.changePassword(changeDto);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByUserName("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testChangePassword_OldPasswordIncorrect() {
        // Arrange
        ChangeMdpDto changeDto = new ChangeMdpDto();
        changeDto.setUsername("testuser");
        changeDto.setOldPassword("wrongPassword");
        changeDto.setNewPassword("newPassword");

        when(userRepository.findByUserName("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);
        doNothing().when(auditClient).createAudit(any(AuditRequestDto.class));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.changePassword(changeDto);
        });

        assertEquals("Old password is incorrect", exception.getMessage());
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verify(auditClient).createAudit(argThat(dto -> 
            dto.getStatus().equals(AuditRequestDto.Status.FAILED.name())
        ));
    }

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUserName("testuser")).thenReturn(testUser);
        when(jwtUtils.generateToken(1L, "testuser", "USER", true)).thenReturn("testToken");
        doNothing().when(auditClient).createAudit(any(AuditRequestDto.class));

        // Act
        AuthResponse response = authService.login(loginUserDto);

        // Assert
        assertNotNull(response);
        assertEquals("testToken", response.getToken());
        assertEquals("testuser", response.getUserName());
        assertEquals("USER", response.getRole());
        assertTrue(response.isFirstLogin());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUserName("testuser");
        verify(jwtUtils).generateToken(1L, "testuser", "USER", true);
        verify(auditClient).createAudit(any(AuditRequestDto.class));
    }

    @Test
    void testLogin_AuthenticationFailed() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.login(loginUserDto);
        });

        assertEquals("Invalid mdp or user", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUserName(anyString());
    }

    @Test
    void testLogin_NotAuthenticated() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            authService.login(loginUserDto);
        });

        assertEquals("Invalid mdp or user", exception.getMessage());
    }

    @Test
    void testUsers_Success() {
        // Arrange
        List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setId(1L);
        user1.setUserName("user1");
        user1.setEmail("user1@example.com");
        user1.setRole(Role.USER);
        users.add(user1);

        User user2 = new User();
        user2.setId(2L);
        user2.setUserName("user2");
        user2.setEmail("user2@example.com");
        user2.setRole(Role.ADMIN);
        users.add(user2);

        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserDto> result = authService.users();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUserName());
        assertEquals("user2", result.get(1).getUserName());
        verify(userRepository).findAll();
    }

    @Test
    void testOtherUsers_Success() {
        // Arrange
        List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setId(2L);
        user1.setUserName("user2");
        user1.setEmail("user2@example.com");
        user1.setRole(Role.USER);
        users.add(user1);

        when(userRepository.findOtherNonAdminUsers(1L)).thenReturn(users);

        // Act
        List<UserDto> result = authService.otherUsers(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user2", result.get(0).getUserName());
        verify(userRepository).findOtherNonAdminUsers(1L);
    }

    @Test
    void testUserName_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        String result = authService.userName(1L);

        // Assert
        assertEquals("testuser", result);
        verify(userRepository).findById(1L);
    }

    @Test
    void testUserName_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            authService.userName(999L);
        });

        assertEquals("User not found with id 999", exception.getMessage());
        verify(userRepository).findById(999L);
    }
}
