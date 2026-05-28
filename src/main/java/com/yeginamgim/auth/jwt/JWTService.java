package com.yeginamgim.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JWTService {
    @Value("${jwt.secret}")
    private String secret;

    private Key secretKey;

    // 비밀키 생성
    @PostConstruct
    public void init(){this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));}

    // [1] 토큰 발급
    public String createToken(String email){
        String token = Jwts.builder()
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration( new Date(System.currentTimeMillis()+(1000L*60*60)))
                .signWith(secretKey, SignatureAlgorithm.HS256) // 토큰에 비밀키 넣고 서명 알고리즘
                .compact();
        return token;
    }
    // [2] 토큰 검증 및 이메일 추출
    public String getClaim(String token){
        if(token != null && token.startsWith("Bearer ")){
            // 토큰이 "Bearer " 로 시작한다면 "Bearer "을 잘라내고 토큰 남겨라
            token = token.substring(7);
        }
        try{
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey) // 비밀키 대입
                    .build() // 비밀키가 일치하지 않으면 예외 발생
                    .parseClaimsJws(token) // 서명 확인할 토큰 대입
                    .getBody(); // 서명확인 토큰 내 클레임(내용물) 반환 / 없으면 예외 발생
            Object object = claims.get("email"); // 클레임(내용물)의 값은 모두 Object 이다.
            return (String)object;
        }catch (Exception e){
            System.out.println(e);
        }
        return null; // 토큰이 없거나 유효하지 않을 때
    }
}
