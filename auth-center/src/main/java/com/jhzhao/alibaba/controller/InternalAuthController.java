package com.jhzhao.alibaba.controller;

import com.jhzhao.alibaba.entity.user.User;
import com.jhzhao.alibaba.service.audit.AuditLogService;
import com.jhzhao.alibaba.service.user.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 内部系统认证控制器
 * 提供用户名密码登录获取 Token 的接口（供内部微服务使用）
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class InternalAuthController {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final AuditLogService auditLogService;

    /**
     * 内部系统登录接口
     * POST /api/auth/login
     *
     * @param loginRequest 登录请求
     * @param request HTTP 请求
     * @return Token 响应
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        log.info("Internal login attempt: username={}, clientId={}",
                loginRequest.getUsername(), loginRequest.getClientId());

        try {
            // 1. 加载用户信息
            User user = (User) userDetailsService.loadUserByUsername(loginRequest.getUsername());

            // 2. 验证密码
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("密码错误");
            }

            // 3. 生成 Access Token
            String accessToken = generateAccessToken(user, loginRequest.getClientId());

            // 4. 生成 Refresh Token
            String refreshToken = generateRefreshToken(user, loginRequest.getClientId());

            // 5. 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("access_token", accessToken);
            response.put("refresh_token", refreshToken);
            response.put("token_type", "Bearer");
            response.put("expires_in", 1800); // 30分钟
            response.put("scope", "internal");

            // 用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("roles", user.getRoles());
            userInfo.put("permissions", user.getPermissions());
            response.put("user_info", userInfo);

            // 6. 记录审计日志
            auditLogService.logLogin(user.getUsername(), loginRequest.getClientId(), true, null);

            log.info("Internal login success: username={}", loginRequest.getUsername());

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Internal login failed: username={}, reason={}",
                    loginRequest.getUsername(), e.getMessage());
            auditLogService.logLogin(loginRequest.getUsername(), loginRequest.getClientId(), false, e.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("error", "invalid_credentials");
            error.put("error_description", e.getMessage());
            return ResponseEntity.status(401).body(error);

        } catch (Exception e) {
            log.error("Internal login error: username={}", loginRequest.getUsername(), e);
            auditLogService.logLogin(loginRequest.getUsername(), loginRequest.getClientId(), false, e.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("error", "server_error");
            error.put("error_description", "登录处理失败");
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 刷新 Token 接口
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(
            @Valid @RequestBody RefreshRequest refreshRequest) {

        log.info("Token refresh attempt");

        try {
            // TODO: 实现 Refresh Token 验证和新的 Access Token 生成
            // 这里简化处理，实际应该解析 refresh token 并验证

            Map<String, Object> error = new HashMap<>();
            error.put("error", "not_implemented");
            error.put("error_description", "Refresh token endpoint not fully implemented");
            return ResponseEntity.status(501).body(error);

        } catch (Exception e) {
            log.error("Token refresh error", e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "invalid_token");
            error.put("error_description", "Refresh token invalid or expired");
            return ResponseEntity.status(401).body(error);
        }
    }

    /**
     * 生成 Access Token
     */
    private String generateAccessToken(User user, String clientId) {
        Instant now = Instant.now();

        // 收集角色和权限
        Set<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        Set<String> roles = user.getRoles();
        Set<String> permissions = user.getPermissions();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("http://localhost:8083")
                .issuedAt(now)
                .expiresAt(now.plus(30, ChronoUnit.MINUTES))
                .subject(user.getUsername())
                .id(UUID.randomUUID().toString())
                .claim("user_id", user.getId())
                .claim("nickname", user.getNickname())
                .claim("email", user.getEmail())
                .claim("client_id", clientId != null ? clientId : "internal")
                .claim("grant_type", "internal_login")
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("scope", "internal")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * 生成 Refresh Token
     */
    private String generateRefreshToken(User user, String clientId) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("http://localhost:8083")
                .issuedAt(now)
                .expiresAt(now.plus(7, ChronoUnit.DAYS))
                .subject(user.getUsername())
                .id(UUID.randomUUID().toString())
                .claim("user_id", user.getId())
                .claim("client_id", clientId != null ? clientId : "internal")
                .claim("token_type", "refresh_token")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * 登录请求 DTO
     */
    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;

        private String clientId;
    }

    /**
     * 刷新 Token 请求 DTO
     */
    @Data
    public static class RefreshRequest {
        @NotBlank(message = "Refresh Token 不能为空")
        private String refreshToken;
    }
}
