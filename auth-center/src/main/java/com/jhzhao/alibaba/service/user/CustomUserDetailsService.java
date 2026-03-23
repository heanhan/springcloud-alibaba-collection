package com.jhzhao.alibaba.service.user;

import com.jhzhao.alibaba.entity.user.User;
import com.jhzhao.alibaba.repository.rbac.PermissionRepository;
import com.jhzhao.alibaba.repository.rbac.RoleRepository;
import com.jhzhao.alibaba.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * 自定义 UserDetailsService
 * 从数据库加载用户及其 RBAC 权限信息
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /**
     * 根据用户名加载用户信息 (包含角色和权限)
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        // 1. 加载用户基本信息
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        // 2. 检查用户状态
        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        // 3. 加载用户角色
        Set<String> roleCodes = roleRepository.findRoleCodesByUserId(user.getId());
        roleCodes.forEach(user::addRole);
        log.debug("User {} roles: {}", username, roleCodes);

        // 4. 加载用户权限 (通过角色关联)
        Set<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(user.getId());
        user.addPermissions(permissionCodes);
        log.debug("User {} permissions: {}", username, permissionCodes);

        return user;
    }

    /**
     * 根据用户ID加载用户信息
     */
    @Transactional(readOnly = true)
    public User loadUserById(Long userId) {
        log.debug("Loading user by id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + userId));

        // 加载角色和权限
        Set<String> roleCodes = roleRepository.findRoleCodesByUserId(user.getId());
        roleCodes.forEach(user::addRole);

        Set<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(user.getId());
        user.addPermissions(permissionCodes);

        return user;
    }
}
