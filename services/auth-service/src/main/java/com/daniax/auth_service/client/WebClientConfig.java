package com.daniax.auth_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient auditWebClient(
            @Value("${client.audit.service}") String auditUrl
    ) {
        return WebClient.builder()
                .baseUrl(auditUrl)
                .build();
    }
}
