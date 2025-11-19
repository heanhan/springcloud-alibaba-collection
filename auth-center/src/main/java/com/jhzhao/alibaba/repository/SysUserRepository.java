package com.jhzhao.alibaba.repository;

import com.jhzhao.alibaba.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Author zhaojh0912
 * Description 用户表
 * CreateDate 2025/11/15 13:29
 * Version 1.0
 */
public interface SysUserRepository extends JpaRepository<SysUser, Long>, JpaSpecificationExecutor<SysUser> {
    Optional<SysUser> findByUsername(String username);
}
