package com.jhzhao.alibaba.service;


import com.jhzhao.alibaba.entity.SysUser;
import com.jhzhao.alibaba.model.dto.LoginResponse;
import com.jhzhao.alibaba.model.dto.RefreshResponse;

/**
 * 认证中心核心服务接口
 * 包含：登录、刷新 Token、登出、权限缓存管理
 * 所有方法均支持 Spring 事务 + 日志 + 异常处理
 */
public interface AuthService {

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码（明文）
     * @return 登录响应（含 token、refreshToken、用户信息、权限）
     * @throws org.springframework.security.authentication.BadCredentialsException 密码错误
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException 用户不存在
     * @throws org.springframework.security.authentication.DisabledException 用户被禁用
     */
    LoginResponse login(String username, String password);

    /**
     * 刷新访问令牌
     * @param refreshToken 旧的刷新令牌
     * @return 新的访问令牌响应
     * @throws io.jsonwebtoken.ExpiredJwtException refreshToken 已过期
     * @throws io.jsonwebtoken.JwtException refreshToken 无效
     */
    RefreshResponse refreshToken(String refreshToken);

    /**
     * 用户登出
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean logout(Long userId);

    /**
     * 清除用户权限缓存（权限变更后调用）
     * @param userId 用户ID
     */
    void clearPermissionCache(Long userId);

    /**
     * 批量清除权限缓存（管理员批量修改权限时使用）
     * @param userIds 用户ID集合
     */
    void clearPermissionCacheBatch(java.util.Collection<Long> userIds);

    /**
     * 验证 Token 是否有效（可选，供网关使用）
     * @param token JWT Token
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 获取当前登录用户信息（从 SecurityContext）
     * @return 当前用户（含权限）
     */
    SysUser  getCurrentUser();
}