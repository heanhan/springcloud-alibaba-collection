package com.jhzhao.alibaba.controller;

import com.jhzhao.alibaba.entity.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户信息控制器
 * 提供 OIDC UserInfo 端点和当前用户信息查询
 */
@RestController
@Slf4j
public class UserInfoController {

    /**
     * OIDC UserInfo 端点
     * GET /userinfo
     */
    @GetMapping("/userinfo")
    public Map<String, Object> userInfo(Authentication authentication) {
        log.debug("UserInfo request for: {}", authentication.getName());

        Map<String, Object> userInfo = new HashMap<>();

        if (authentication.getPrincipal() instanceof User user) {
            // 基本用户信息
            userInfo.put("sub", user.getUsername());
            userInfo.put("preferred_username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("email", user.getEmail());
            userInfo.put("email_verified", user.getEmail() != null);

            // 头像
            if (user.getAvatar() != null) {
                userInfo.put("picture", user.getAvatar());
            }

            // 角色信息
            Set<String> roles = user.getRoles();
            if (roles != null && !roles.isEmpty()) {
                userInfo.put("roles", roles);
            }

            // 权限信息
            Set<String> permissions = user.getPermissions();
            if (permissions != null && !permissions.isEmpty()) {
                userInfo.put("permissions", permissions);
            }

        } else if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            // OAuth2User 处理
            userInfo.putAll(oauth2User.getAttributes());
        } else {
            // 默认处理
            userInfo.put("sub", authentication.getName());
            userInfo.put("preferred_username", authentication.getName());
        }

        return userInfo;
    }

    /**
     * 获取当前登录用户信息
     * GET /api/user/me
     */
    @GetMapping("/api/user/me")
    public Map<String, Object> currentUser(Authentication authentication) {
        log.debug("Current user request for: {}", authentication.getName());

        Map<String, Object> result = new HashMap<>();

        if (authentication.getPrincipal() instanceof User user) {
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("nickname", user.getNickname());
            result.put("email", user.getEmail());
            result.put("phone", user.getPhone());
            result.put("avatar", user.getAvatar());
            result.put("enabled", user.isEnabled());
            result.put("roles", user.getRoles());
            result.put("permissions", user.getPermissions());
        } else {
            result.put("username", authentication.getName());
            result.put("authorities", authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .collect(Collectors.toList()));
        }

        return result;
    }
}
