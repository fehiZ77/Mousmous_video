package com.moustass.transactions_service.filter;

import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {
    @Value("${app.secret-key}")
    private String secretKey;

    @Value("${app.expiration-time}")
    private long expirationTime;

    public Map<String, Object> tokenData(String token){
        Map<String, Object> result = new HashMap<>();
        boolean isTokenExpired = extractAllClaims(token).getExpiration().before(new Date());
        result.put("isTokenExpired", isTokenExpired);
        if(!isTokenExpired){
            String userName = extractAllClaims(token).getSubject();
            String role = extractAllClaims(token).get("role", String.class);

            result.put("userName", userName);
            result.put("role", role);
        }
        return result;
    }

    private Claims extractAllClaims(String token) {
        Key key = new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS256.getJcaName());
        return Jwts.parserBuilder()
                .setSigningKey(key) // même clé que pour signer
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
