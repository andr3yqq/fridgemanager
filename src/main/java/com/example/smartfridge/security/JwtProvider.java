package com.example.smartfridge.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey signingKey;
    private final JwtParser jwtParser;
    @Value("${JWT_EXPIRATION_TIME}")
    private Long expirationTime;

    public JwtProvider(@Value("${JWT_SECRET}") String secret) {
        byte[] decodedSecret = Decoders.BASE64.decode(secret);

        this.signingKey = Keys.hmacShaKeyFor(decodedSecret);
        this.jwtParser = Jwts.parser()
                .verifyWith(signingKey)
                .build();
    }

    public String generateToken(String user) {
        return Jwts.builder()
                .subject(user)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(signingKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return jwtParser
                .parseSignedClaims(token)
                .getPayload();
    }
}
