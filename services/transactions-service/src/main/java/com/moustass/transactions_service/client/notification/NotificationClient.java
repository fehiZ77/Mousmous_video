package com.moustass.transactions_service.client.notification;

import com.moustass.transactions_service.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class NotificationClient {
    private final WebClient webClient;

    public NotificationClient(@Qualifier("notificationWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public void createNotification(NotificationRequestDto notificationRequestDto){
        String token = SecurityUtil.getCurrentToken();

        webClient.post()
                .uri("/create")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(notificationRequestDto)
                .retrieve()
                .toBodilessEntity()   // on ne récupère pas de body car void
                .block();
        
    }
}
