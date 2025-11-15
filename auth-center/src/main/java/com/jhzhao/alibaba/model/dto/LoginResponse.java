package com.jhzhao.alibaba.model.dto;

import com.jhzhao.alibaba.entity.SysUser;
import lombok.Data;

/**
 * 登录成功响应体
 * 对应接口：POST /api/auth/login
 * 返回字段说明：
 * - token: 访问令牌（用于后续 API 请求）
 * - refreshToken: 刷新令牌（用于刷新 token）
 * - userId: 用户ID
 * - username: 用户名
 * - nickname: 昵称（显示用）
 * - avatar: 头像 URL
 * - permissions: 权限列表（可选，前端路由守卫用）
 */
@Data
public class LoginResponse {

    /** 访问令牌（JWT） */
    private String token;

    /** 刷新令牌（用于刷新 token） */
    private String refreshToken;

    /** 用户ID */
    private Long userId;

    /** 用户名（登录名） */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 权限列表（RBAC） */
    private java.util.List<String> permissions;

    /**
     * 构造器：从 SysUser 构建
     */
    public LoginResponse(String token, String refreshToken, SysUser user, java.util.List<String> permissions) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = user.getId();
        this.username = user.getUsername();
        this.nickname = user.getNickname();
        this.avatar = user.getAvatar();
        this.permissions = permissions != null ? permissions : java.util.Collections.emptyList();
    }

    /**
     * 无权限版本（简化）
     */
    public LoginResponse(String token, String refreshToken, SysUser user) {
        this(token, refreshToken, user, null);
    }
}
