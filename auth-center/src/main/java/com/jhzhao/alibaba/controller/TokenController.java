package com.jhzhao.alibaba.controller;

import com.jhzhao.alibaba.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {

    private final JwtService jwtService;

    private final RedisTemplate<String, String> redisTemplate;

    @PostMapping("/refresh")
    public JsonResult<Map<String, String>> refresh(@RequestParam String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);
        if (username != null && jwtService.isTokenValid(refreshToken, username, true)) {
            String newAccessToken = jwtService.generateToken(username);
            Map<String, String> tokens = Map.of(
                    "access_token", newAccessToken,
                    "refresh_token", refreshToken,
                    "expires_in", "7200"
            );
            return JsonResult.ok(tokens);
        }
        throw new BadCredentialsException("无效的刷新令牌");
    }
}