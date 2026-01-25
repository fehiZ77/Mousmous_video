package com.daniax.auth_service.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    private String secretKey = "testSecretKeyForJwtTokenGenerationAndValidation123456789";
    private long expirationTime = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtils, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtUtils, "expirationTime", expirationTime);
    }

    @Test
    void testGenerateToken_Success() {
        // Arrange
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";
        boolean isFirstLogin = true;

        // Act
        String token = jwtUtils.generateToken(userId, username, role, isFirstLogin);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Verify token can be parsed
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        assertEquals(username, claims.getSubject());
        assertEquals(userId.longValue(), ((Number) claims.get("userId")).longValue());
        assertEquals(role, claims.get("role"));
        assertEquals(isFirstLogin, claims.get("isFirstLogin"));
    }

    @Test
    void testExtractUsername_Success() {
        // Arrange
        String username = "testuser";
        String token = jwtUtils.generateToken(1L, username, "USER", true);

        // Act
        String extractedUsername = jwtUtils.extractUsername(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    void testValidateToken_ValidToken() {
        // Arrange
        String username = "testuser";
        String token = jwtUtils.generateToken(1L, username, "USER", true);
        
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(username);

        // Act
        Boolean isValid = jwtUtils.validateToken(token, userDetails);

        // Assert
        assertTrue(isValid);
        verify(userDetails).getUsername();
    }

    @Test
    void testValidateToken_InvalidUsername() {
        // Arrange
        String username = "testuser";
        String token = jwtUtils.generateToken(1L, username, "USER", true);
        
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("differentuser");

        // Act
        Boolean isValid = jwtUtils.validateToken(token, userDetails);

        // Assert
        assertFalse(isValid);
        verify(userDetails).getUsername();
    }

    @Test
    void testValidateToken_ExpiredToken() throws InterruptedException {
        // Arrange
        ReflectionTestUtils.setField(jwtUtils, "expirationTime", 1000L); // 1 second
        
        String username = "testuser";
        String token = jwtUtils.generateToken(1L, username, "USER", true);
        
        // Wait for token to expire
        Thread.sleep(1100);
        
        UserDetails userDetails = mock(UserDetails.class);

        // Act
        Boolean isValid = jwtUtils.validateToken(token, userDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testGenerateToken_DifferentRoles() {
        // Arrange
        String tokenUser = jwtUtils.generateToken(1L, "user1", "USER", false);
        String tokenAdmin = jwtUtils.generateToken(2L, "admin1", "ADMIN", false);

        // Act & Assert
        Claims claimsUser = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(tokenUser)
                .getBody();
        
        Claims claimsAdmin = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(tokenAdmin)
                .getBody();

        assertEquals("USER", claimsUser.get("role"));
        assertEquals("ADMIN", claimsAdmin.get("role"));
    }

    @Test
    void testGenerateToken_FirstLoginFlag() {
        // Arrange
        String tokenFirstLogin = jwtUtils.generateToken(1L, "user1", "USER", true);
        String tokenNotFirstLogin = jwtUtils.generateToken(2L, "user2", "USER", false);

        // Act & Assert
        Claims claimsFirst = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(tokenFirstLogin)
                .getBody();
        
        Claims claimsNotFirst = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(tokenNotFirstLogin)
                .getBody();

        assertEquals(true, claimsFirst.get("isFirstLogin"));
        assertEquals(false, claimsNotFirst.get("isFirstLogin"));
    }

    private Key getSignKey() {
        byte[] keyBytes = secretKey.getBytes();
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }
}
