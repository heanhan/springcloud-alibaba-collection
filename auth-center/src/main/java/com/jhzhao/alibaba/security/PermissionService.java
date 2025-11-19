package com.jhzhao.alibaba.security;

import com.jhzhao.alibaba.entity.SysMenu;
import com.jhzhao.alibaba.entity.SysRoleMenu;
import com.jhzhao.alibaba.entity.SysUserRole;
import com.jhzhao.alibaba.repository.SysMenuRepository;
import com.jhzhao.alibaba.repository.SysRoleMenuRepository;
import com.jhzhao.alibaba.repository.SysUserRoleRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionService {


    @Resource
    private SysUserRoleRepository userRoleRepository;

    @Resource
    private SysMenuRepository menuRepository;

    @Resource
    private SysRoleMenuRepository roleMenuRepository;

    /**
     * 获取用户的所有权限
     * @param username 用户名
     * @return 用户权限列表
     */
    public List<String> getUserPermissions(String username) {
        // 1. 获取用户角色
        List<SysUserRole> userRoles = userRoleRepository.findByUserId(getUserIdByUsername(username));

        // 2. 获取角色权限
        Set<Long> collect = userRoles.stream().map(item -> item.getRoleId()).collect(Collectors.toSet());
        List<String> rolePermissions = getRolePermissions(collect);
        return rolePermissions;
    }

    private Long getUserIdByUsername(String username) {
        // 这里需要实现获取用户ID的方法，实际项目中需要查询数据库
        // 为了简化，假设我们有一个方法可以获取用户ID
        return 1L; // 示例，实际需要根据username查询
    }

    private List<String> getRolePermissions(Set<Long> roleId) {
        // 1. 获取角色拥有的菜单
        List<SysRoleMenu> roleMenus = roleMenuRepository.findByRoleIds(roleId);

        // 2. 获取菜单的权限
        return roleMenus.stream()
                .map(roleMenu -> {
                    SysMenu menu = menuRepository.findById(roleMenu.getMenuId()).orElse(null);
                    return menu != null ? menu.getPermission() : null;
                })
                .filter(permission -> permission != null)
                .collect(Collectors.toList());
    }
}
