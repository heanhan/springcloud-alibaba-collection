package com.jhzhao.alibaba.security;

import com.jhzhao.alibaba.entity.SysUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author zhaojh0912
 * Description jwt的生成和解析工具
 * CreateDate 2025/11/15 13:31
 * Version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    // ==================== 配置项（支持 Nacos 热更新） ====================
    @Value("${jwt.secret}")
    private String secret;  // 生产必须 256 位以上

    @Value("${jwt.expiration:86400000}")
    private long expirationMs;  // 默认 24 小时

    // ==================== 依赖注入 ====================
    private final PermissionService permissionService;

    // ==================== 核心方法 ====================

    /**
     * 生成 JWT Token（包含权限）
     */
    public String generateToken(SysUser user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("用户不能为空");
        }

        // 1. 加载权限（从 Redis 缓存）
        Set<String> permissions = permissionService.getUserPermissions(user.getId());

        // 2. 构建 Claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("nickname", user.getNickname());
        claims.put("permissions", new ArrayList<>(permissions));  // 关键：权限列表
        claims.put("iat", System.currentTimeMillis());

        // 3. 生成 Token
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * 解析 JWT Token
     */
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT 已过期: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            throw new RuntimeException("无效的 Token", e);
        }
    }

    /**
     * 获取用户名
     */
    public String getUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * 获取用户ID
     */
    public Long getUserId(String token) {
        Object userId = extractAllClaims(token).get("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    /**
     * 获取权限列表
     */
    public List<String> getPermissions(String token) {
        Claims claims = extractAllClaims(token);
        Object perms = claims.get("permissions");
        if (perms instanceof List<?> list) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean isTokenValid(String token, String username) {
        try {
            String tokenUsername = getUsername(token);
            return username.equals(tokenUsername) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractAllClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // ==================== 内部工具 ====================

    /**
     * 获取签名密钥（HS512 要求 256 位）
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 64) {
            log.warn("JWT 密钥长度不足，建议至少 64 字节（512 位）");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ==================== 刷新 Token（可选） ====================

    /**
     * 刷新 Token（保持权限）
     */
    public String refreshToken(String oldToken) {
        Claims claims = extractAllClaims(oldToken);
        Long userId = Long.valueOf(claims.get("userId").toString());

        // 重新加载最新权限
        SysUser user = new SysUser();
        user.setId(userId);
        user.setUsername(claims.getSubject());
        user.setNickname((String) claims.get("nickname"));

        return generateToken(user);
    }
}