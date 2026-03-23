package com.jhzhao.alibaba.entity.rbac;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 系统权限实体类
 */
@Entity
@Table(name = "sys_permission")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permission_code", unique = true, nullable = false, length = 100)
    private String permissionCode;

    @Column(name = "permission_name", nullable = false, length = 100)
    private String permissionName;

    @Column(name = "resource_type", length = 50)
    private String resourceType;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(length = 200)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 拥有此权限的角色编码集合 (不持久化，通过代码查询维护)
     */
    @Transient
    private Set<String> roleCodes = new HashSet<>();

    /**
     * 子权限ID集合 (不持久化，通过代码查询维护)
     */
    @Transient
    private Set<Long> childrenIds = new HashSet<>();

    /**
     * 添加角色编码
     */
    public void addRoleCode(String roleCode) {
        if (this.roleCodes == null) {
            this.roleCodes = new HashSet<>();
        }
        this.roleCodes.add(roleCode);
    }

    /**
     * 获取角色编码集合
     */
    public Set<String> getRoleCodes() {
        return this.roleCodes != null ? this.roleCodes : new HashSet<>();
    }
}
