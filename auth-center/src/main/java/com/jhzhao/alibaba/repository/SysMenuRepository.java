package com.jhzhao.alibaba.repository;

import com.jhzhao.alibaba.entity.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

/**
 * Author zhaojh0912
 * Description 菜单表
 * CreateDate 2025/11/15 13:30
 * Version 1.0
 */
public interface SysMenuRepository extends JpaRepository<SysMenu, Long>, JpaSpecificationExecutor<SysMenu> {

    @Query("SELECT DISTINCT m.permission FROM SysMenu m JOIN SysRoleMenu rm ON m.id = rm.menuId JOIN SysRole r ON r.id = rm.roleId WHERE r.roleCode IN :roleCodes AND m.permission IS NOT NULL")
    Set<String> findPermissionsByRoleCodes(Set<Long> roleCodes);

}
