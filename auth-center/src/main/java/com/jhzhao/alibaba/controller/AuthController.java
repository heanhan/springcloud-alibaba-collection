package com.jhzhao.alibaba.controller;

import com.jhzhao.alibaba.model.vo.LoginUserVO;
import com.jhzhao.alibaba.result.ResultBody;
import com.jhzhao.alibaba.security.TokenService;
import com.jhzhao.alibaba.utils.RedisUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private TokenService tokenService;

    @RestController
    @RequestMapping("/auth")
    @RequiredArgsConstructor
    public class AuthController {

        @Resource
        private AuthenticationManager authManager;

        @Resource
        private TokenService tokenService;

        @Resource
        private TokenCache tokenCache;

        @PostMapping("/login")
        public ResultBody<Map<String, String>> login(@RequestBody LoginUserVO dto) {
            try {
                Authentication auth = authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
                );

                String username = auth.getName();
                String accessToken = tokenService.generateAccessToken(username);
                String refreshToken = tokenService.generateRefreshToken(username);

                tokenCache.saveAccess(username, accessToken, 7200);
                tokenCache.saveRefresh(username, refreshToken, 86400);

                return ResultBody.success(Map.of(
                        "access_token", accessToken,
                        "refresh_token", refreshToken
                ));
            } catch (BadCredentialsException e) {
                return ResultBody.error("用户名或密码错误");
            }
        }

        @PostMapping("/logout")
        public ResultBody<Void> logout(HttpServletRequest request) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                String username = tokenService.getUsernameFromToken(token);
                tokenCache.deleteTokens(username);
            }
            return ResultBody.success();
        }

        @PostMapping("/refresh")
        public ResultBody<Map<String, String>> refresh(@RequestBody RefreshDTO dto) {
            String oldRefresh = dto.getRefresh_token();
            if (!jwtUtil.validate(oldRefresh) || !jwtUtil.isRefresh(oldRefresh)) {
                return ResultBody.error("非法 refresh_token");
            }

            String username = jwtUtil.getUsername(oldRefresh);
            String cached = tokenCache.getRefresh(username);
            if (!oldRefresh.equals(cached)) {
                return ResultBody.error("refresh_token 已失效");
            }

            String newAccess = jwtUtil.generateAccessToken(username);
            String newRefresh = jwtUtil.generateRefreshToken(username);

            tokenCache.saveAccess(username, newAccess, 7200);
            tokenCache.saveRefresh(username, newRefresh, 86400);

            return ResultBody.success(Map.of(
                    "access_token", newAccess,
                    "refresh_token", newRefresh
            ));
        }
    }
}