package com.jhzhao.alibaba.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhzhao.alibaba.entity.SysUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2025/11/15 13:31
 * Version 1.0
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private final PermissionService permissionService;

    public String generateToken(SysUser user) {
        Set<String> permissions = permissionService.getUserPermissions(user.getId());

        Map<String, Object> claims = new HashMap<>();
        claims.put("permissions", new ArrayList<>(permissions));
        claims.put("userId", user.getId());
        claims.put("nickname", user.getNickname());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    public List<String> getPermissionsFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.getOrDefault("permissions", Collections.emptyList());
    }

    public String getUsername(String token) {
        return extractAllClaims(token).getSubject();
    }
}