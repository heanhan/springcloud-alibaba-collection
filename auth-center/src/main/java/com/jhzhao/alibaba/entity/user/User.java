package com.jhzhao.alibaba.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统用户实体类
 * 实现 Spring Security 的 UserDetails 接口
 */
@Entity
@Table(name = "sys_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable, UserDetails {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 200)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(length = 200)
    private String avatar;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "account_non_expired", nullable = false)
    private Boolean accountNonExpired = true;

    @Column(name = "account_non_locked", nullable = false)
    private Boolean accountNonLocked = true;

    @Column(name = "credentials_non_expired", nullable = false)
    private Boolean credentialsNonExpired = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 用户角色列表 (不持久化，运行时加载)
     */
    @Transient
    private Set<String> roles = new HashSet<>();

    /**
     * 用户权限列表 (不持久化，运行时加载)
     */
    @Transient
    private Set<String> permissions = new HashSet<>();

    /**
     * 获取用户权限集合 (Spring Security)
     * 返回权限字符串集合，如: user:read, order:write
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> allAuthorities = new HashSet<>();

        // 添加角色 (ROLE_ 前缀)
        if (roles != null) {
            roles.forEach(role -> allAuthorities.add("ROLE_" + role));
        }

        // 添加权限
        if (permissions != null) {
            allAuthorities.addAll(permissions);
        }

        return allAuthorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired != null ? accountNonExpired : true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked != null ? accountNonLocked : true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired != null ? credentialsNonExpired : true;
    }

    @Override
    public boolean isEnabled() {
        return enabled != null ? enabled : true;
    }

    /**
     * 添加角色
     */
    public void addRole(String role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    /**
     * 添加权限
     */
    public void addPermission(String permission) {
        if (this.permissions == null) {
            this.permissions = new HashSet<>();
        }
        this.permissions.add(permission);
    }

    /**
     * 批量添加权限
     */
    public void addPermissions(Collection<String> perms) {
        if (perms != null) {
            perms.forEach(this::addPermission);
        }
    }

    /**
     * 清空权限和角色
     */
    public void clearAuthorities() {
        if (this.roles != null) {
            this.roles.clear();
        }
        if (this.permissions != null) {
            this.permissions.clear();
        }
    }
}
