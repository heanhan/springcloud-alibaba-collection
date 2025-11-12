package com.jhzhao.alibaba.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "sys_role_menu")
//@IdClass(RoleMenuId.class)
@Data
public class SysRoleMenu {
    @Id
    private Long roleId;

    @Id
    private Long menuId;
}