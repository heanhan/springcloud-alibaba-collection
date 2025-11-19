package com.jhzhao.alibaba.security;


import com.jhzhao.alibaba.repository.SysUserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    @Value("${jwt.access-token-expiration:7200}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:8640000}")
    private Long refreshTokenExpiration;

    @Value("${jwt.secret-key:auth-center}")
    private String secretKey;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SysUserRepository userRepository;

    private SecretKey getSigningKey() {
        // 确保密钥长度足够（至少32字节用于HS256）
        byte[] keyBytes = secretKey.getBytes();
        if (keyBytes.length < 32) {
            // 扩展密钥长度
            String extendedKey = String.format("%-32s", secretKey).replace(' ', '0');
            keyBytes = extendedKey.getBytes();
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Map<String, String> generateToken(String username) {
        // 生成access token
        String accessToken = generateAccessToken(username);
        // 生成refresh token
        String refreshToken = generateRefreshToken(username);

        // 将token存入Redis，设置过期时间
        redisTemplate.opsForValue().set("auth:access_token:" + username, accessToken, accessTokenExpiration, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set("auth:refresh_token:" + username, refreshToken, refreshTokenExpiration, TimeUnit.MILLISECONDS);

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("access_token", accessToken);
        tokenMap.put("refresh_token", refreshToken);

        return tokenMap;
    }

    private String generateAccessToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateAccessToken(String accessToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(accessToken);
            return true;
        } catch (Exception e) {
            System.out.println("验证访问令牌时发生错误: " + e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(refreshToken);
            return true;
        } catch (Exception e) {
            System.out.println("验证刷新令牌时发生错误: " + e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            System.out.println("从令牌获取用户名时发生错误: " + e.getMessage());
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            System.out.println("检查令牌过期时发生错误: " + e.getMessage());
            return true; // 如果无法解析，认为已过期
        }
    }

    public boolean isTokenValid(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            boolean valid = (username.equals(tokenUsername) && !isTokenExpired(token));
            return valid;
        } catch (Exception e) {
            System.out.println("验证令牌有效性时发生错误: " + e.getMessage());
            return false;
        }
    }

    // 添加getter方法以便在其他地方使用
    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
}



