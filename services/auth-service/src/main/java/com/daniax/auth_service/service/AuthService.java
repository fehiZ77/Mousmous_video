package com.daniax.auth_service.service;

import com.daniax.auth_service.configuration.JwtUtils;
import com.daniax.auth_service.dto.AuthResponse;
import com.daniax.auth_service.dto.ChangeMdpDto;
import com.daniax.auth_service.dto.LoginUserDto;
import com.daniax.auth_service.dto.UserDto;
import com.daniax.auth_service.entity.User;
import com.daniax.auth_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public User create(User user) throws Exception{
        if(userRepository.findByUserName(user.getUserName()) != null) throw new Exception("User already exists");

        user.setMdp(passwordEncoder.encode(user.getMdp()));
        return userRepository.save(user);
    }

    public void changePassword(ChangeMdpDto change) throws Exception {

        User user = userRepository.findByUserName(change.getUsername());
        if (user == null) {
            throw new Exception("User not found");
        }

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(change.getOldPassword(), user.getMdp())) {
            throw new Exception("Old password is incorrect");
        }

        user.setMdp(passwordEncoder.encode(change.getNewPassword()));
        user.setFirstLogin(false);
        userRepository.save(user);
    }

    public AuthResponse login(LoginUserDto loginUserDto) throws Exception{
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginUserDto.getUserName(), loginUserDto.getMdp()));
        if(authentication.isAuthenticated()){
            User user = userRepository.findByUserName(loginUserDto.getUserName());

            // Générer le token
            String token = jwtUtils.generateToken(user.getId(), user.getUserName(), user.getRole().name(), user.getFirstLogin());

            return new AuthResponse(
                    token,
                    user.getUserName(),
                    user.getRole().name(),
                    user.getFirstLogin()
            );
        }
        throw new Exception("Invalid mdp or user");
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
