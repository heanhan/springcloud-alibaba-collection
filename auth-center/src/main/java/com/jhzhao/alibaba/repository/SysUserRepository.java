package com.jhzhao.alibaba.repository;

import com.jhzhao.alibaba.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2025/11/15 13:29
 * Version 1.0
 */
public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);
}
