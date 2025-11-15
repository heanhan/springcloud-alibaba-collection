package com.jhzhao.alibaba.security;

import com.jhzhao.alibaba.entity.SysMenu;
import com.jhzhao.alibaba.entity.SysRoleMenu;
import com.jhzhao.alibaba.entity.SysUserRole;
import com.jhzhao.alibaba.repository.SysMenuRepository;
import com.jhzhao.alibaba.repository.SysRoleMenuRepository;
import com.jhzhao.alibaba.repository.SysUserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2025/11/15 13:51
 * Version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final SysUserRoleRepository userRoleRepository;
    private final SysRoleMenuRepository roleMenuRepository;
    private final SysMenuRepository menuRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY = "auth:permissions:user:%s";

    public Set<String> getUserPermissions(Long userId) {
        String key = String.format(CACHE_KEY, userId);
        Set<String> cached = (Set<String>) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }

        Set<String> permissions = new HashSet<>();

        List<Long> roleIds = userRoleRepository.findByUserId(userId).stream()
                .map(SysUserRole::getRoleId)
                .toList();

        if (roleIds.isEmpty()) {
            cache(userId, permissions);
            return permissions;
        }

        List<Long> menuIds = roleMenuRepository.findAllById(
                roleIds.stream().map(r -> new RoleMenuId(r, null)).toList()
        ).stream().map(SysRoleMenu::getMenuId).toList();

        if (menuIds.isEmpty()) {
            cache(userId, permissions);
            return permissions;
        }

        List<String> perms = menuRepository.findAllById(menuIds).stream()
                .map(SysMenu::getPermission)
                .filter(StringUtils::hasText)
                .toList();

        permissions.addAll(perms);
        cache(userId, permissions);

        log.info("加载用户 {} 权限: {}", userId, permissions);
        return permissions;
    }

    private void cache(Long userId, Set<String> permissions) {
        String key = String.format(CACHE_KEY, userId);
        redisTemplate.opsForValue().set(key, permissions, 30, TimeUnit.MINUTES);
    }

    public void clearCache(Long userId) {
        redisTemplate.delete(String.format(CACHE_KEY, userId));
    }
}
