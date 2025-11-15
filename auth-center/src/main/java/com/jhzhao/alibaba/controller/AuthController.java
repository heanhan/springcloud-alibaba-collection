package com.jhzhao.alibaba.controller;

import com.jhzhao.alibaba.service.AuthService;
import com.jhzhao.alibaba.service.impl.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2025/11/15 13:32
 * Version 1.0
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public R<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse resp = authService.login(request.getUsername(), request.getPassword());
        return R.ok(resp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        return R.ok("登出成功");
    }
}

@Data
class LoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}