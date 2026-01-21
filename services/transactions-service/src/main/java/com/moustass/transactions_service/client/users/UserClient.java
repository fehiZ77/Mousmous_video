package com.moustass.transactions_service.client.users;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class UserClient {
    private final WebClient webClient;

    public UserClient(@Qualifier("usersWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public String getUserName(Long userId, String token) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/nameUser")
                        .queryParam("userId", userId)
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // synchrone
    }
}
