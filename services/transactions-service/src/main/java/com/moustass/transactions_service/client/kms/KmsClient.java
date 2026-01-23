package com.moustass.transactions_service.client.kms;

import com.moustass.transactions_service.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class KmsClient {

    private final WebClient webClient;

    public KmsClient(@Qualifier("kmsWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public String signFile(Long userId, Long keyId, String fileHash) {
        KmsSignRequest req = new KmsSignRequest();
        req.setUserId(userId);
        req.setKeyId(keyId);
        req.setFileHash(fileHash);

        String token = SecurityUtil.getCurrentToken();

        return webClient.post()
                .uri("/sign")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // synchrone
    }

    public boolean verifyFile(String pk, String fileHash, String signatureFile){
        KmsVerifyRequest req = new KmsVerifyRequest(
                pk,
                fileHash,
                signatureFile
        );

        String token = SecurityUtil.getCurrentToken();

        return Boolean.TRUE.equals(webClient.post()
                .uri("/verify")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block()); // synchrone
    }
}
