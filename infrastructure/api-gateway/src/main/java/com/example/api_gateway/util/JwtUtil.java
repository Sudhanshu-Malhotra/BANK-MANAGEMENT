package com.example.api_gateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;

@Component
public class JwtUtil {

    public static final String SECRET = "afafasfafafasfasfasfafacasdasfasxASFACASDFACASDFASFASFDAFACA";

    public void validateToken(final String token) {
        Jwts.parser().verifyWith((javax.crypto.SecretKey) getSignKey()).build().parseSignedClaims(token);
    }

    public String extractUsername(String token) {
        return Jwts.parser().verifyWith((javax.crypto.SecretKey) getSignKey()).build().parseSignedClaims(token).getPayload().getSubject();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
