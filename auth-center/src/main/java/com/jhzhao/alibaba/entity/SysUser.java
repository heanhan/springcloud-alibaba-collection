package com.jhzhao.alibaba.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户表 sys_user
 */

@Entity
@Table(name = "sys_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysUser implements Serializable, UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;//用户名

    @Column(nullable = false)
    private String password;//密码

    private String nickname;//昵称

    private String avatar;//头像

    private String email;//邮箱

    @Column(nullable = false)
    private Boolean enabled = true;//是否启用

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;//创建时间

    @UpdateTimestamp
    private LocalDateTime updateTime;//更新时间
    // ==================== UserDetails 接口实现 ====================

    /**
     * 返回用户权限集合（RBAC 权限标识，如 user:view）
     * 注意：这里返回的是权限字符串，不是角色
     */
    @Transient // 不持久化到数据库
    private Set<String> permissions = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream()
                .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    // 以下状态默认启用，可根据业务扩展字段控制
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    // ==================== 业务方法 ====================

    /**
     * 给当前用户添加权限（用于登录时注入）
     */
    public void addPermission(String permission) {
        if (StringUtils.hasText(permission)) {
            this.permissions.add(permission);
        }
    }

    /**
     * 批量添加权限
     */
    public void addPermissions(Collection<String> perms) {
        perms.forEach(this::addPermission);
    }

    /**
     * 清空权限（登出、刷新时使用）
     */
    public void clearPermissions() {
        this.permissions.clear();
    }
}
