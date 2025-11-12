package com.jhzhao.alibaba.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

// SysMenu (权限/菜单)
@Entity
@Table(name = "sys_menu")
@Data
public class SysMenu implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String permission;

    private String path;

    private String component;

    private String name;

    private String icon;

    private Integer sort;

    private Boolean keepAlive;

    private Boolean hidden;

    private Long parentId;
}