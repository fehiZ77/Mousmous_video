package com.daniax.auth_service.service;

import com.daniax.auth_service.AuthException.GlobalException;
import com.daniax.auth_service.client.audit.AuditAction;
import com.daniax.auth_service.client.audit.AuditClient;
import com.daniax.auth_service.client.audit.AuditRequestDto;
import com.daniax.auth_service.configuration.JwtUtils;
import com.daniax.auth_service.dto.*;
import com.daniax.auth_service.entity.User;
import com.daniax.auth_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuditClient auditClient;

    private static final String SERVICE_NAME = "AUTH";

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils,
            AuditClient auditClient)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.auditClient = auditClient;
    }

    public User create(User user) throws GlobalException {
        
        if(userRepository.findByUserName(user.getUserName()) != null) {
            new AuditRequestDto(
                    SERVICE_NAME,
                    AuditAction.USER_CREATED.name(),
                    "",
                    AuditRequestDto.Status.FAILED,
                    LocalDateTime.now().toString()
            );
            throw new GlobalException("User already exists");
        }

        user.setMdp(passwordEncoder.encode(user.getMdp()));
        user =  userRepository.save(user);

        auditClient.createAudit(
                new AuditRequestDto(
                        SERVICE_NAME,
                        AuditAction.USER_CREATED.name(),
                        "New user : " +user.getUserName(),
                        AuditRequestDto.Status.SUCCES,
                        LocalDateTime.now().toString()
                )
        );
        return user;
    }

    public void changePassword(ChangeMdpDto change) throws GlobalException {
        User user = userRepository.findByUserName(change.getUsername());
        if (user == null) {
            throw new GlobalException("User not found");
        }

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(change.getOldPassword(), user.getMdp())) {
            auditClient.createAudit(
                    new AuditRequestDto(
                            SERVICE_NAME,
                            AuditAction.CHANGE_MDP.name(),
                            "",
                            AuditRequestDto.Status.FAILED,
                            LocalDateTime.now().toString()
                    )
            );
            throw new GlobalException("Old password is incorrect");
        }

        user.setMdp(passwordEncoder.encode(change.getNewPassword()));
        user.setFirstLogin(false);
        userRepository.save(user);
        auditClient.createAudit(
                new AuditRequestDto(
                        SERVICE_NAME,
                        AuditAction.CHANGE_MDP.name(),
                        "",
                        AuditRequestDto.Status.SUCCES,
                        LocalDateTime.now().toString()
                )
        );
    }

    public AuthResponse login(LoginUserDto loginUserDto) throws GlobalException{
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginUserDto.getUserName(), loginUserDto.getMdp())
            );
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new GlobalException("Invalid mdp or user");
        }
        if(authentication.isAuthenticated()){
            User user = userRepository.findByUserName(loginUserDto.getUserName());

            // Générer le token
            String token = jwtUtils.generateToken(user.getId(), user.getUserName(), user.getRole().name(), user.getFirstLogin());

            AuditRequestDto audit = new AuditRequestDto(
                    SERVICE_NAME,
                    AuditAction.USER_LOGIN.name(),
                    "",
                    AuditRequestDto.Status.SUCCES,
                    LocalDateTime.now().toString()
            );
            audit.setToken(token);
            auditClient.createAudit(audit);

            return new AuthResponse(
                    token,
                    user.getUserName(),
                    user.getRole().name(),
                    user.getFirstLogin()
            );
        }
        throw new GlobalException("Invalid mdp or user");
    }

    public List<UserDto> users(){
        List<User> temp = userRepository.findAll();
        List<UserDto> result = new ArrayList<>();
        for (User u : temp){
            result.add(new UserDto(u.getId(), u.getUserName(),u.getEmail(), u.getRole().name()));
        }
        return result;
    }

    public List<UserDto> otherUsers(Long userId){
        List<User> temp = userRepository.findOtherNonAdminUsers(userId);
        List<UserDto> result = new ArrayList<>();
        for (User u : temp){
            result.add(new UserDto(u.getId(), u.getUserName(),u.getEmail(), u.getRole().name()));
        }
        return result;
    }

    public String userName(Long userId){
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()){
            return user.get().getUserName();
        }else{
            throw new EntityNotFoundException("User not found with id " + userId);
        }

    }
}
