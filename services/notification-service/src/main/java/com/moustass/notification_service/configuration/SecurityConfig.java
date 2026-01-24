package com.moustass.notification_service.configuration;

import com.moustass.notification_service.filter.JwtConfig;
import com.moustass.notification_service.filter.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    private final JwtUtils jwtUtils;

    public SecurityConfig(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(kms ->
                        kms.anyRequest().authenticated())
                // kms.anyRequest().permitAll()) // Decomment on debug mode
                .addFilterBefore(new JwtConfig(jwtUtils), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
