package com.jhzhao.alibaba.repository.rbac;

import com.jhzhao.alibaba.entity.rbac.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户角色关联 Repository
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    /**
     * 根据用户ID查询所有关联
     */
    List<UserRole> findByUserId(Long userId);

    /**
     * 根据角色ID查询所有关联
     */
    List<UserRole> findByRoleId(Long roleId);

    /**
     * 删除用户的所有角色关联
     */
    void deleteByUserId(Long userId);

    /**
     * 检查用户是否拥有指定角色
     */
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
}
