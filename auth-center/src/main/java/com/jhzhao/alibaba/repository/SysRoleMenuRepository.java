package com.jhzhao.alibaba.repository;

import com.jhzhao.alibaba.entity.SysRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

/**
 * Author zhaojh0912
 * Description 角色菜单的中间表
 * CreateDate 2025/11/15 13:30
 * Version 1.0
 */
public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, Long>, JpaSpecificationExecutor<SysRoleMenu> {

    @Query(value = " from SysRoleMenu srm where srm.roleId in ?1")
    List<SysRoleMenu> findByRoleIds(Set<Long> roleIds);
}
