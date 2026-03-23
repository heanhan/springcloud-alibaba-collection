package com.jhzhao.alibaba.repository.rbac;

import com.jhzhao.alibaba.entity.rbac.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 权限 Repository
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 根据权限编码查找权限
     */
    Optional<Permission> findByPermissionCode(String permissionCode);

    /**
     * 根据权限编码集合查找权限
     */
    List<Permission> findByPermissionCodeIn(Set<String> permissionCodes);

    /**
     * 根据用户ID查询用户的所有权限
     */
    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN RolePermission rp ON p.id = rp.permissionId " +
           "JOIN UserRole ur ON rp.roleId = ur.roleId " +
           "JOIN Role r ON rp.roleId = r.id " +
           "WHERE ur.userId = :userId AND r.enabled = true")
    List<Permission> findPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询用户的所有权限编码
     */
    @Query("SELECT DISTINCT p.permissionCode FROM Permission p " +
           "JOIN RolePermission rp ON p.id = rp.permissionId " +
           "JOIN UserRole ur ON rp.roleId = ur.roleId " +
           "JOIN Role r ON rp.roleId = r.id " +
           "WHERE ur.userId = :userId AND r.enabled = true")
    Set<String> findPermissionCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询角色的所有权限
     */
    @Query("SELECT p FROM Permission p JOIN RolePermission rp ON p.id = rp.permissionId WHERE rp.roleId = :roleId")
    List<Permission> findPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 检查权限编码是否存在
     */
    boolean existsByPermissionCode(String permissionCode);
}
