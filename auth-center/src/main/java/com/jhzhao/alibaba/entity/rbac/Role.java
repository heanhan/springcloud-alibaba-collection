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
 * 系统角色实体类
 */
@Entity
@Table(name = "sys_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_code", unique = true, nullable = false, length = 50)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 角色拥有的权限编码集合 (不持久化，通过代码查询维护)
     */
    @Transient
    private Set<String> permissionCodes = new HashSet<>();

    /**
     * 添加权限编码
     */
    public void addPermissionCode(String permissionCode) {
        if (this.permissionCodes == null) {
            this.permissionCodes = new HashSet<>();
        }
        this.permissionCodes.add(permissionCode);
    }

    /**
     * 批量添加权限编码
     */
    public void addPermissionCodes(Set<String> codes) {
        if (codes != null) {
            codes.forEach(this::addPermissionCode);
        }
    }

    /**
     * 获取所有权限编码
     */
    public Set<String> getPermissionCodes() {
        return this.permissionCodes != null ? this.permissionCodes : new HashSet<>();
    }
}
