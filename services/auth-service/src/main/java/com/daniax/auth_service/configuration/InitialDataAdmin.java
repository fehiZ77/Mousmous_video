package com.daniax.auth_service.configuration;

import com.daniax.auth_service.entity.Role;
import com.daniax.auth_service.entity.User;
import com.daniax.auth_service.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialDataAdmin {

    @Value("${app.admin-mdp}")
    private String adminMdp;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public InitialDataAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initDefaultAdmin(){
        String adminName = "admin";

        if(userRepository.findByUserName(adminName) == null){
            User admin = new User();
            admin.setUserName(adminName);
            admin.setEmail("admin@admin.com");
            admin.setFirstLogin(false);
            admin.setMdp(passwordEncoder.encode(adminMdp));
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
        }
    }
}
