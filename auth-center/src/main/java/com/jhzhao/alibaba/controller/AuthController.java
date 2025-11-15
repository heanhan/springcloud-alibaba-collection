package com.jhzhao.alibaba.controller;

import com.jhzhao.alibaba.result.ResultBody;
import com.jhzhao.alibaba.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    public ResultBody login(@RequestBody @Valid String name) {
//        LoginResponse resp = authService.login(request.getUsername(), request.getPassword());
        return ResultBody.success();
    }

    @PostMapping("/logout")
    public ResultBody logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        return ResultBody.success();
    }
}
