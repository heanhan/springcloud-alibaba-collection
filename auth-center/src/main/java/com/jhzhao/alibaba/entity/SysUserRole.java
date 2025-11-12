package com.jhzhao.alibaba.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

// 中间表
@Entity
@Table(name = "sys_user_role")
//@IdClass(UserRoleId.class)
@Data
public class SysUserRole implements Serializable {
    @Id
    private Long userId;

    @Id
    private Long roleId;
}