package com.jhzhao.alibaba.repository;

import com.jhzhao.alibaba.entity.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Author zhaojh0912
 * Description 用户角色中间表的持久层
 * CreateDate 2025/11/15 13:30
 * Version 1.0
 */
public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Long>, JpaSpecificationExecutor<SysUserRole> {

    /**
     * 通过用户id 进行查询用户角色中间表
     * @param userId
     * @return
     */
    List<SysUserRole> findByUserId(Long userId);
}