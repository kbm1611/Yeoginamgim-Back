package com.yeginamgim.auth.jwt;

import com.yeginamgim.global.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JWTService {
    @Value("${jwt.secret}")
    private String secret;

    private Key secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(String email) {
        return Jwts.builder()
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (1000L * 60 * 60)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getClaim(String token) {
        if (!StringUtils.hasText(token)) {
            throw new InvalidTokenException();
        }
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(rawToken)
                    .getBody();
            Object emailClaim = claims.get("email");
            if (!(emailClaim instanceof String email) || !StringUtils.hasText(email)) {
                throw new InvalidTokenException();
            }
            return email;
        } catch (InvalidTokenException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidTokenException();
        }
    }

    public String extractEmailFromBearerToken(String bearerToken) {
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            throw new InvalidTokenException();
        }
        return getClaim(bearerToken);
    }
}
