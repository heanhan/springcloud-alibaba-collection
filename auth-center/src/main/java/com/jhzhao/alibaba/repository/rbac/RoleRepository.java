package com.jhzhao.alibaba.repository.rbac;

import com.jhzhao.alibaba.entity.rbac.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 角色 Repository
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据角色编码查找角色
     */
    Optional<Role> findByRoleCode(String roleCode);

    /**
     * 根据角色编码集合查找角色
     */
    List<Role> findByRoleCodeIn(Set<String> roleCodes);

    /**
     * 根据用户ID查询用户的所有角色
     */
    @Query("SELECT r FROM Role r JOIN UserRole ur ON r.id = ur.roleId WHERE ur.userId = :userId AND r.enabled = true")
    List<Role> findRolesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询用户的所有角色编码
     */
    @Query("SELECT r.roleCode FROM Role r JOIN UserRole ur ON r.id = ur.roleId WHERE ur.userId = :userId AND r.enabled = true")
    Set<String> findRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 检查角色编码是否存在
     */
    boolean existsByRoleCode(String roleCode);
}
