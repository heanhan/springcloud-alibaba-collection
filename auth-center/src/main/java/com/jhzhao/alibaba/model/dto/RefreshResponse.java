package com.jhzhao.alibaba.model.dto;

import lombok.Data;

/**
 * Token 刷新响应体
 * 对应接口：POST /api/auth/refresh
 *
 * 返回字段说明：
 * - token: 新的访问令牌（JWT）
 * - refreshToken: 新的刷新令牌（可选，建议与 token 一致或更长有效期）
 * - expiresIn: 过期时间（秒）
 * - tokenType: 令牌类型（固定为 "Bearer"）
 */
@Data
public class RefreshResponse {

    /** 新的访问令牌（JWT） */
    private String token;

    /** 新的刷新令牌（用于下次刷新） */
    private String refreshToken;

    /** 过期时间（秒） */
    private Long expiresIn;

    /** 令牌类型（固定为 Bearer） */
    private String tokenType = "Bearer";

    /**
     * 构造器：从新 Token 构建
     */
    public RefreshResponse(String token, String refreshToken, long expiresInSeconds) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresInSeconds;
    }

    /**
     * 简化构造器（refreshToken = token）
     */
    public RefreshResponse(String token, long expiresInSeconds) {
        this(token, token, expiresInSeconds);
    }
}
