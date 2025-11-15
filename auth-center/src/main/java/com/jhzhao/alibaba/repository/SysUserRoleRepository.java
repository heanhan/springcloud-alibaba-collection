package com.jhzhao.alibaba.repository;

import com.jhzhao.alibaba.entity.SysUserRole;

import java.util.List;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2025/11/15 13:30
 * Version 1.0
 */
public interface SysUserRoleRepository extends JpaRepository<SysUserRole, UserRoleId> {
    List<SysUserRole> findByUserId(Long userId);
}