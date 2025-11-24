package com.jhzhao.alibaba.repository;

import com.jhzhao.alibaba.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

/**
 * Author zhaojh0912
 * Description 用户表
 * CreateDate 2025/11/15 13:29
 * Version 1.0
 */
public interface SysUserRepository extends JpaRepository<SysUser, Long>, JpaSpecificationExecutor<SysUser> {

    /**
     * 通过用户名查询用户
     * @param username  账号/用户名
     * @return
     */
    SysUser findByUsername(String username);
    
    //查询用户角色编码
    @Query(value = """
        SELECT r.role_code
        FROM sys_role r
        JOIN sys_user_role ur ON r.id = ur.role_id
        WHERE ur.user_id = :userId
        """, nativeQuery = true)
    Set<String> findRoleCodesByUserId(@Param("userId") Long userId);

    //查询用户权限字符串（permission）
    @Query(value = """
        SELECT DISTINCT m.permission
        FROM sys_menu m
        JOIN sys_role_menu rm ON m.id = rm.menu_id
        JOIN sys_user_role ur ON rm.role_id = ur.role_id
        WHERE ur.user_id = :userId
          AND m.permission IS NOT NULL
        """, nativeQuery = true)
    Set<String> findPermissionsByUserId(@Param("userId") Long userId);


}
