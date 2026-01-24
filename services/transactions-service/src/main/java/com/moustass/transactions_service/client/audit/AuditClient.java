package com.moustass.transactions_service.client.audit;

import com.moustass.transactions_service.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AuditClient {
    private final WebClient webClient;

    public AuditClient(@Qualifier("auditWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public void createAudit(AuditRequestDto audit) {

        if(audit.getToken() == null || audit.getToken().equals("")){    // We need this for ths
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
