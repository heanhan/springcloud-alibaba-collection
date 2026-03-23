package com.jhzhao.alibaba.repository.rbac;

import com.jhzhao.alibaba.entity.rbac.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色权限关联 Repository
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    /**
     * 根据角色ID查询所有关联
     */
    List<RolePermission> findByRoleId(Long roleId);

    /**
     * 根据权限ID查询所有关联
     */
    List<RolePermission> findByPermissionId(Long permissionId);

    /**
     * 删除角色的所有权限关联
     */
    void deleteByRoleId(Long roleId);

    /**
     * 检查角色是否拥有指定权限
     */
    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);
}
