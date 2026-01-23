package com.daniax.auth_service.client.audit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.daniax.auth_service.utils.SecurityUtil;

@Service
public class AuditClient {

    private final WebClient webClient;

    public AuditClient(@Qualifier("auditWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public void createAudit(AuditRequestDto audit) {

        if(audit.getToken() == null || audit.getToken().equals("")){
            String token = SecurityUtil.getCurrentToken();
            audit.setToken(token);
        }

        webClient.post()
                .uri("/create")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + audit.getToken())
                .bodyValue(audit)
                .retrieve()
                .toBodilessEntity()   // on ne récupère pas de body car void
                .block();
    }

}
