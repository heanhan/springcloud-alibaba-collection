package com.jhzhao.alibaba.controller;

import com.jhzhao.alibaba.enums.CommonEnum;
import com.jhzhao.alibaba.model.vo.LoginUserVO;
import com.jhzhao.alibaba.result.ResultBody;
import com.jhzhao.alibaba.security.TokenService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private TokenService tokenService;

    @PostMapping("/login")
    public ResultBody login(@RequestBody LoginUserVO loginUserVO) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginUserVO.getUsername(), loginUserVO.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 生成token
            Map<String, String> tokens = tokenService.generateToken(loginUserVO.getUsername());
            Map<String, Object> result = new HashMap<>();
            result.put("access_token", tokens.get("access_token"));
            result.put("refresh_token", tokens.get("refresh_token"));

            return ResultBody.success(result);
        } catch (Exception e) {
            return ResultBody.error(CommonEnum.INVALID_USERNAME_PASSWORD);
        }
    }

    @PostMapping("/logout")
    public ResultBody logout() {
        // 退出时清除token，这里我们只是返回成功
        return ResultBody.success("退出成功");
    }

    @PostMapping("/refresh")
    public ResultBody refresh(@RequestParam String refreshToken) {
        // 验证refresh token
        if (!tokenService.validateRefreshToken(refreshToken)) {
            return ResultBody.error("刷新token无效");
        }

        String username = tokenService.getUsernameFromToken(refreshToken);

        // 生成新的access token
        String newAccessToken = tokenService.generateRefreshToken(username);

        // 更新Redis中的access token
        redisTemplate.opsForValue().set("auth:access_token:" + username, newAccessToken, tokenService.getAccessTokenExpiration(), TimeUnit.MILLISECONDS);

        Map<String, String> result = new HashMap<>();
        result.put("access_token", newAccessToken);

        return ResultBody.success(result);
    }
}