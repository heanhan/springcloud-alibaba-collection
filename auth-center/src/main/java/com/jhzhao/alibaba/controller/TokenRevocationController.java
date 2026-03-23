package com.jhzhao.alibaba.controller;

import com.jhzhao.alibaba.service.audit.AuditLogService;
import com.jhzhao.alibaba.service.oauth2.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Token 撤销控制器
 * 实现 OAuth 2.0 Token Revocation (RFC 7009)
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class TokenRevocationController {

    private final OAuth2AuthorizationService authorizationService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtDecoder jwtDecoder;
    private final AuditLogService auditLogService;

    /**
     * Token 撤销端点
     * POST /oauth2/revoke
     *
     * @param token         要撤销的 Token
     * @param tokenTypeHint Token 类型提示 (access_token 或 refresh_token)
     * @param authentication 当前认证信息
     * @return 撤销结果
     */
    @PostMapping("/oauth2/revoke")
    public ResponseEntity<Map<String, Object>> revokeToken(
            @RequestParam(OAuth2ParameterNames.TOKEN) String token,
            @RequestParam(value = OAuth2ParameterNames.TOKEN_TYPE_HINT, required = false) String tokenTypeHint,
            Authentication authentication,
            HttpServletRequest request) {

        log.info("Token revocation request received");

        Map<String, Object> response = new HashMap<>();

        if (!StringUtils.hasText(token)) {
            response.put("error", "invalid_request");
            response.put("error_description", "Token is required");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 尝试解析 JWT Token
            Jwt jwt = jwtDecoder.decode(token);
            String tokenId = jwt.getId();  // JTI
            String username = jwt.getSubject();
            String clientId = jwt.getClaimAsString("client_id");
            Date expirationTime = Date.from(jwt.getExpiresAt());

            // 确定 Token 类型
            String tokenType = determineTokenType(tokenTypeHint, jwt);

            // 1. 将 Token 加入黑名单
            tokenBlacklistService.addToBlacklist(
                    tokenId != null ? tokenId : token,
                    tokenType,
                    username,
                    clientId,
                    expirationTime
            );

            // 2. 从授权服务中移除授权信息
            removeAuthorization(token);

            // 3. 记录审计日志
            auditLogService.logTokenRevoke(username, clientId, tokenType, true);

            log.info("Token revoked successfully: user={}, client={}, type={}",
                    username, clientId, tokenType);

            // RFC 7009: 成功响应返回 200 OK，响应体可选
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.warn("Failed to decode token, treating as opaque token: {}", e.getMessage());

            // 对于不透明 Token，尝试从授权服务中查找
            boolean removed = removeAuthorization(token);

            if (removed) {
                log.info("Opaque token revoked successfully");
                return ResponseEntity.ok(response);
            } else {
                // Token 不存在或已过期，根据 RFC 7009 仍返回成功
                log.info("Token not found or already expired, returning success per RFC 7009");
                return ResponseEntity.ok(response);
            }
        }
    }

    /**
     * 确定 Token 类型
     */
    private String determineTokenType(String tokenTypeHint, Jwt jwt) {
        if (StringUtils.hasText(tokenTypeHint)) {
            return tokenTypeHint;
        }

        // 根据 JWT Claims 推断
        // 如果有 scope 且包含 openid，可能是 ID Token
        // 这里简化处理，默认 access_token
        return "access_token";
    }

    /**
     * 从授权服务中移除授权信息
     */
    private boolean removeAuthorization(String token) {
        try {
            // 尝试查找并移除 Access Token 对应的授权
            OAuth2Authorization authorization = authorizationService.findByToken(
                    token, OAuth2TokenType.ACCESS_TOKEN);

            if (authorization != null) {
                authorizationService.remove(authorization);
                log.debug("Authorization removed for access token");
                return true;
            }

            // 尝试查找 Refresh Token
            authorization = authorizationService.findByToken(
                    token, OAuth2TokenType.REFRESH_TOKEN);

            if (authorization != null) {
                authorizationService.remove(authorization);
                log.debug("Authorization removed for refresh token");
                return true;
            }

            return false;
        } catch (Exception e) {
            log.warn("Error removing authorization: {}", e.getMessage());
            return false;
        }
    }
}
