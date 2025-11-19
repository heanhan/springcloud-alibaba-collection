package com.jhzhao.alibaba.repository;

import com.jhzhao.alibaba.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Author zhaojh0912
 * Description 角色表
 * CreateDate 2025/11/15 13:29
 * Version 1.0
 */
@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, Long>, JpaSpecificationExecutor<SysRole> {

}
