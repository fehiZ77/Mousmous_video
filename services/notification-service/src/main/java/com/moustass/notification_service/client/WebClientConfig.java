package com.moustass.notification_service.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient usersWebClient(
            @Value("${client.users.service}") String usersUrl
    ) {
        return WebClient.builder()
                .baseUrl(usersUrl)
                .build();
    }
}
