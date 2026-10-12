package com.fieldnotes.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long ttlMillis;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-hours}") long hours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlMillis = hours * 3_600_000L;
    }

    public String generate(Long userId) {
        long now = System.currentTimeMillis();
        return Jwts.builder().subject(String.valueOf(userId))
                .issuedAt(new Date(now)).expiration(new Date(now + ttlMillis))
                .signWith(key).compact();
    }

    /** Returns the user id or throws a JwtException if the token is invalid/expired. */
    public Long parse(String token) {
        String sub = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
        return Long.valueOf(sub);
    }
}
