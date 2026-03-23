package com.jhzhao.alibaba.security;

import com.jhzhao.alibaba.entity.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 自定义 JWT Token 定制器
 * 在 JWT 中注入 RBAC 相关的 Claims (roles, permissions, user_id 等)
 */
@Slf4j
public class CustomJwtTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERMISSIONS = "permissions";
    private static final String CLAIM_USER_ID = "user_id";
    private static final String CLAIM_NICKNAME = "nickname";
    private static final String CLAIM_EMAIL = "email";

    @Override
    public void customize(JwtEncodingContext context) {
        JwtClaimsSet.Builder claims = context.getClaims();

        // 为所有 Token 类型添加 JTI (JWT ID)
        claims.id(UUID.randomUUID().toString());

        // 获取当前认证信息
        Authentication authentication = context.getPrincipal();

        // 根据不同的 Token 类型定制 Claims
        if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            customizeAccessToken(context, claims, authentication);
        } else if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
            customizeIdToken(context, claims, authentication);
        }
    }

    /**
     * 定制 Access Token
     */
    private void customizeAccessToken(JwtEncodingContext context, JwtClaimsSet.Builder claims, Authentication authentication) {
        log.debug("Customizing Access Token for client: {}", context.getRegisteredClient().getClientId());

        // 获取用户主体
        User user = extractUser(authentication);

        if (user != null) {
            // 添加用户基本信息
            claims.claim(CLAIM_USER_ID, user.getId());
            claims.claim(CLAIM_NICKNAME, user.getNickname());
            claims.claim(CLAIM_EMAIL, user.getEmail());

            // 添加角色信息
            Set<String> roles = user.getRoles();
            if (roles != null && !roles.isEmpty()) {
                claims.claim(CLAIM_ROLES, roles);
            }

            // 添加权限信息
            Set<String> permissions = user.getPermissions();
            if (permissions != null && !permissions.isEmpty()) {
                claims.claim(CLAIM_PERMISSIONS, permissions);
            }

            log.debug("Access Token claims added: user_id={}, roles={}, permissions={}",
                    user.getId(), roles, permissions);
        }

        // 添加客户端信息
        claims.claim("client_id", context.getRegisteredClient().getClientId());
        claims.claim("grant_type", context.getAuthorizationGrantType().getValue());
    }

    /**
     * 定制 ID Token (OpenID Connect)
     */
    private void customizeIdToken(JwtEncodingContext context, JwtClaimsSet.Builder claims, Authentication authentication) {
        log.debug("Customizing ID Token for client: {}", context.getRegisteredClient().getClientId());

        User user = extractUser(authentication);

        if (user != null) {
            // ID Token 标准 Claims
            claims.claim("preferred_username", user.getUsername());
            claims.claim("nickname", user.getNickname());

            if (user.getEmail() != null) {
                claims.claim("email", user.getEmail());
                claims.claim("email_verified", true);
            }

            if (user.getAvatar() != null) {
                claims.claim("picture", user.getAvatar());
            }

            // 添加角色信息 (可选，根据 OIDC 规范)
            Set<String> roles = user.getRoles();
            if (roles != null && !roles.isEmpty()) {
                claims.claim(CLAIM_ROLES, roles);
            }
        }
    }

    /**
     * 从 Authentication 中提取 User 对象
     */
    private User extractUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }

        // 处理 UsernamePasswordAuthenticationToken
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            Object authPrincipal = ((UsernamePasswordAuthenticationToken) authentication).getPrincipal();
            if (authPrincipal instanceof User) {
                return (User) authPrincipal;
            }
        }

        return null;
    }

    /**
     * 从 Authorities 中提取角色
     */
    private Set<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))  // 移除 ROLE_ 前缀
                .collect(Collectors.toSet());
    }

    /**
     * 从 Authorities 中提取权限
     */
    private Set<String> extractPermissions(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toSet());
    }
}
