package com.jhzhao.alibaba.controller;

import com.jhzhao.alibaba.entity.SysUser;
import com.jhzhao.alibaba.model.vo.LoginUserVO;
import com.jhzhao.alibaba.model.vo.RefreshVO;
import com.jhzhao.alibaba.model.vo.UserRegisterVO;
import com.jhzhao.alibaba.result.ResultBody;
import com.jhzhao.alibaba.security.TokenCache;
import com.jhzhao.alibaba.security.TokenService;
import com.jhzhao.alibaba.service.SysUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Resource
    private AuthenticationManager authManager;

    @Resource
    private TokenService tokenService;

    @Resource
    private TokenCache tokenCache;

    @Resource
    private SysUserService sysUserService;

    /**
     * 用户注册
     * @param request
     * @return
     */
    @PostMapping("/register")
    public ResultBody register(@Valid @RequestBody UserRegisterVO request) {
        SysUser user = sysUserService.register(request);
        return ResultBody.success(user.getUsername()+": 账号创建成功！");
    }


    @PostMapping("/getToken")
    public ResultBody<Map<String, String>> getToken(@RequestBody LoginUserVO dto) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
            );
            String username = auth.getName();
            Map<String, String> stringStringMap = tokenService.generateToken(username);
            return ResultBody.success(stringStringMap);
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

    @PostMapping("/refreshToken")
    public ResultBody<Map<String, String>> refreshToken(@RequestBody RefreshVO dto) {
        String oldRefresh = dto.getRefresh_token();
        if (!tokenService.validateRefreshToken(oldRefresh)) {
            return ResultBody.error("非法 refresh_token");
        }
        String username = tokenService.getUsernameFromToken(oldRefresh);
        String cached = tokenCache.getRefresh(username);
        if (!oldRefresh.equals(cached)) {
            return ResultBody.error("refresh_token 已失效");
        }
        String newAccess = tokenService.generateAccessToken(username);
        String newRefresh = tokenService.generateRefreshToken(username);
        tokenCache.saveAccess(username, newAccess, 7200);
        tokenCache.saveRefresh(username, newRefresh, 86400);
        return ResultBody.success(Map.of(
                "access_token", newAccess,
                "refresh_token", newRefresh
        ));
    }
}