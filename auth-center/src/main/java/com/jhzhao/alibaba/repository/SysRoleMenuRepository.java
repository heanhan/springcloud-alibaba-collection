package com.jhzhao.alibaba.repository;

import com.jhzhao.alibaba.entity.SysRoleMenu;

import java.util.List;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2025/11/15 13:30
 * Version 1.0
 */
public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, roleId> {
    List<SysRoleMenu> findByRoleId(Long roleId);
}
