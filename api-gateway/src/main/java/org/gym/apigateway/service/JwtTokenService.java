package org.gym.apigateway.service;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;

import java.security.Key;

@Service
public class JwtTokenService {
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    public void validateToken(final String token) {
        Jwts.parser().setSigningKey(getKey()).build().parseClaimsJws(token);
    }

    private Key getKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
