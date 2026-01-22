package com.daniax.auth_service.controller;

import com.daniax.auth_service.dto.AuthResponse;
import com.daniax.auth_service.dto.ChangeMdpDto;
import com.daniax.auth_service.dto.LoginUserDto;
import com.daniax.auth_service.dto.RegisterUserDto;
import com.daniax.auth_service.entity.User;
import com.daniax.auth_service.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginUserDto userDto) {
        try {
            AuthResponse response = authService.login(userDto);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping("/change")
    public ResponseEntity<?> change(@RequestBody ChangeMdpDto mdpDto) {
        try {
            authService.changePassword(mdpDto);
            return new ResponseEntity<>("Mdp changed", HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * Retourne tous les utilisateurs sauf l'utilisateur donné
     * et sauf les ADMIN unh
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/others")
    public ResponseEntity<?> getOtherUsers(@RequestParam Long userId) {
        try {
            return new ResponseEntity<>(authService.otherUsers(userId), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * Retourne le nom d'un user via userId
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/nameUser")
    public ResponseEntity<String> getUserName(@RequestParam Long userId) {
        try {
            return new ResponseEntity<>(authService.userName(userId), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto userDto) {
        try {
            User user = new User(userDto.getUserName(), userDto.getEmail(), userDto.getMdp(), userDto.getRole());
            return new ResponseEntity<>(authService.create(user), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<?> users() {
        try {
            return new ResponseEntity<>(authService.users(), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
