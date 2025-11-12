package com.jhzhao.alibaba.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expire}")
    private long accessExpire;

    @Value("${jwt.refresh-token-expire}")
    private long refreshExpire;

    @Resource
    private redisTemplate redisTemplate;

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpire * 1000))
                .signWith(getSignKey())
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpire * 1000))
                .signWith(getSignKey())
                .claim("type", "refresh")
                .compact();
    }

    public boolean isTokenValid(String token, String username, boolean isRefresh) {
        // 结合 Redis 黑名单校验
        String blackKey = "blacklist:" + token;
        if (redisTemplate.hasKey(blackKey)) return false;

        String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}