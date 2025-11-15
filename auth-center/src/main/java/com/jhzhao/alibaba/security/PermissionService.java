package com.jhzhao.alibaba.security;

import com.jhzhao.alibaba.entity.SysMenu;
import com.jhzhao.alibaba.entity.SysRoleMenu;
import com.jhzhao.alibaba.entity.SysUserRole;
import com.jhzhao.alibaba.repository.SysMenuRepository;
import com.jhzhao.alibaba.repository.SysRoleMenuRepository;
import com.jhzhao.alibaba.repository.SysUserRoleRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2025/11/15 13:51
 * Version 1.0
 */
@Service
@Slf4j
public class PermissionService {

    // ==================== 缓存配置 ====================
    private static final String CACHE_PREFIX = "auth:permissions:user:";
    private static final long CACHE_TTL_MINUTES = 30;

    // ==================== 依赖注入 ====================
    @Resource
    private SysUserRoleRepository userRoleRepository;

    @Resource
    private SysMenuRepository menuRepository;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private SysRoleMenuRepository sysRoleMenuRepository;


    // ==================== 核心方法 ====================

    /**
     * 获取用户所有权限标识（permission 字段）
     * @param userId 用户ID
     * @return 权限集合，如 { "user:view", "user:add" }
     */
    public Set<String> getUserPermissions(Long userId) {
        if (userId == null) {
            log.warn("userId 为空，返回空权限");
            return Collections.emptySet();
        }

        String cacheKey = CACHE_PREFIX + userId;
        Set<String> cachedPermissions = getCachedPermissions(cacheKey);

        if (cachedPermissions != null) {
            log.debug("缓存命中: userId={}", userId);
            return cachedPermissions;
        }

        Set<String> permissions = loadPermissionsFromDatabase(userId);
        cachePermissions(cacheKey, permissions);
        log.info("加载用户权限: userId={} → {}", userId, permissions);
        return permissions;
    }

    /**
     * 清除用户权限缓存（管理员修改权限后调用）
     * @param userId 用户ID
     */
    public void clearCache(Long userId) {
        if (userId == null) return;
        String cacheKey = CACHE_PREFIX + userId;
        Boolean deleted = redisTemplate.delete(cacheKey);
        log.info("清除权限缓存: userId={}，结果: {}", userId, deleted==true?"成功":"失败");
    }

    /**
     * 批量清除缓存（批量更新时使用）
     * @param userIds 用户ID集合
     */
    public void clearCacheBatch(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return;
        Set<String> keys = userIds.stream()
                .map(id -> CACHE_PREFIX + id)
                .collect(Collectors.toSet());
        Long deleted = redisTemplate.delete(keys);
        log.info("批量清除权限缓存: {} 个用户，实际删除: {}", userIds.size(), deleted);
    }

    // ==================== 内部实现 ====================

    /**
     * 从 Redis 读取缓存
     */
    @SuppressWarnings("unchecked")
    private Set<String> getCachedPermissions(String cacheKey) {
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof Set) {
            return (Set<String>) cached;
        }
        return null;
    }

    /**
     * 缓存权限到 Redis
     */
    private void cachePermissions(String cacheKey, Set<String> permissions) {
        try {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    new HashSet<>(permissions), // 防止序列化问题
                    CACHE_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            log.error("缓存权限失败: key={}", cacheKey, e);
        }
    }

    /**
     * 从数据库加载权限（核心 RBAC 逻辑）
     */
    private Set<String> loadPermissionsFromDatabase(Long userId) {
        Set<String> permissions = new HashSet<>();

        // 第一步: 用户 → 角色
        List<Long> roleIds = userRoleRepository.findByUserId(userId).stream()
                .map(SysUserRole::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            log.debug("用户 {} 无角色", userId);
            return permissions;
        }

        // 第二步: 角色 → 菜单
        List<SysRoleMenu> byRoleIds = sysRoleMenuRepository.findByRoleIds(new HashSet<>(roleIds));
        //获取到所有的菜单目录
        Set<Long> menuIds = byRoleIds.stream().map(item -> item.getMenuId()).collect(Collectors.toSet());
        if (menuIds.isEmpty()) {
            log.debug("用户 {} 的角色无菜单", userId);
            return permissions;
        }
        // 第三步: 菜单 → 权限
        List<String> permList = menuRepository.findAllById(menuIds).stream()
                .map(SysMenu::getPermission)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());

        permissions.addAll(permList);
        return permissions;
    }
}
