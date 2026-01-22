package com.moustass.transactions_service.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    @Qualifier("kmsWebClient")
    public WebClient kmsWebClient(
            @Value("${client.kms.service}") String kmsUrl
    ) {
        return WebClient.builder()
                .baseUrl(kmsUrl)
                .build();
    }

    @Bean
    @Qualifier("usersWebClient")
    public WebClient usersWebClient(
            @Value("${client.users.service}") String usersnUrl
    ) {
        return WebClient.builder()
                .baseUrl(usersnUrl)
                .build();
    }
}
