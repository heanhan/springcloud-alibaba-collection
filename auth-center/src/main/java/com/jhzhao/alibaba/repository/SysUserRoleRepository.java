package com.jhzhao.alibaba.repository;

import com.jhzhao.alibaba.entity.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2025/11/15 13:30
 * Version 1.0
 */
public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Long> {
    List<SysUserRole> findByUserId(Long userId);
}