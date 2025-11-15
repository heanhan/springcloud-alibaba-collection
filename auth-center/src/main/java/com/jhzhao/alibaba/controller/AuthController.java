package com.jhzhao.alibaba.controller;

import com.jhzhao.alibaba.model.dto.LoginResponse;
import com.jhzhao.alibaba.model.dto.RefreshResponse;
import com.jhzhao.alibaba.result.ResultBody;
import com.jhzhao.alibaba.security.JwtTokenProvider;
import com.jhzhao.alibaba.model.vo.LoginUserVO;
import com.jhzhao.alibaba.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @Resource
    private AuthService authService;

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    // ==================== 1. 登录接口 ====================

    @PostMapping("/login")
    public ResultBody<LoginResponse> login(@RequestBody @Valid LoginUserVO loginUser) {
        log.info("用户登录请求: username={}", loginUser.getUsername());

        try {
            LoginResponse response = authService.login(loginUser.getUsername(), loginUser.getPassword());
            log.info("登录成功: userId={}", response.getUserId());
            return ResultBody.success(response);
        } catch (Exception e) {
            log.warn("登录失败: {}", e.getMessage());
            return ResultBody.error(401, e.getMessage());
        }
    }

    // ==================== 2. 刷新 Token 接口 ====================

    @PostMapping("/refresh")
    public ResultBody<RefreshResponse> refresh(HttpServletRequest request) {
        String oldToken = extractToken(request);
        if (oldToken == null) {
            return ResultBody.error(401, "未提供 Token");
        }
        try {
            String newToken = jwtTokenProvider.refreshToken(oldToken);
            RefreshResponse resp = new RefreshResponse(newToken,86400000);
            log.info("Token 刷新成功");
            return ResultBody.success(resp);
        } catch (Exception e) {
            log.warn("Token 刷新失败: {}", e.getMessage());
            return ResultBody.error(401, "无效或已过期的 Token");
        }
    }

    // ==================== 3. 退出登录接口 ====================

    @PostMapping("/logout")
    public ResultBody<String> logout(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) {
            try {
                Long userId = jwtTokenProvider.getUserId(token);
                authService.clearPermissionCache(userId);
                log.info("用户登出: userId={}", userId);
            } catch (Exception e) {
                log.warn("登出时解析 Token 失败: {}", e.getMessage());
            }
        }

        // 清除 Spring Security 上下文
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        return ResultBody.success("登出成功");
    }

    // ==================== 工具方法 ====================

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}