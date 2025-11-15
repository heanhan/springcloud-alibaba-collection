package com.jhzhao.alibaba.security;

import com.jhzhao.alibaba.entity.SysUser;
import com.jhzhao.alibaba.repository.SysUserRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;


/**
 * Author zhaojh0912
 * Description 用于实现UserDetailsService 通过用户名查询用户是否存在
 * CreateDate 2025/11/15 13:31
 * Version 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private SysUserRepository userRepository;

    @Resource
    private PermissionService permissionService;

    /**
     * Spring Security 核心方法：根据用户名加载用户
     * 直接返回 SysUser（它已实现 UserDetails）
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 从数据库查询用户
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        // 2. 检查用户状态
        if (!user.getEnabled()) {
            throw new UsernameNotFoundException("用户已被禁用");
        }

        // 3. 动态加载权限（RBAC）
        try {
            Set<String> permissions = permissionService.getUserPermissions(user.getId());
            user.clearPermissions();           // 防止重复
            user.addPermissions(permissions);  // 注入权限到 UserDetails
            log.debug("用户 {} 加载权限: {}", username, permissions);
        } catch (Exception e) {
            log.error("加载用户权限失败: {}", username, e);
            // 降级：不抛异常，允许登录但无权限
        }
        // 4. 返回 SysUser（已实现 UserDetails）
        return user;
    }
}