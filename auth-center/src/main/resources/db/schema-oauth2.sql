-- ========================================================
-- OAuth 2.1 Authorization Server 官方表结构
-- Spring Authorization Server 1.5.0
-- ========================================================

-- 客户端注册表
CREATE TABLE IF NOT EXISTS oauth2_registered_client (
    id varchar(100) NOT NULL,
    client_id varchar(100) NOT NULL,
    client_id_issued_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    client_secret varchar(200) DEFAULT NULL,
    client_secret_expires_at timestamp DEFAULT NULL,
    client_name varchar(200) NOT NULL,
    client_authentication_methods varchar(1000) NOT NULL,
    authorization_grant_types varchar(1000) NOT NULL,
    redirect_uris varchar(1000) DEFAULT NULL,
    post_logout_redirect_uris varchar(1000) DEFAULT NULL,
    scopes varchar(1000) NOT NULL,
    client_settings varchar(2000) NOT NULL,
    token_settings varchar(2000) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY client_id_unique (client_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OAuth2 客户端注册表';

-- 授权信息表
CREATE TABLE IF NOT EXISTS oauth2_authorization (
    id varchar(100) NOT NULL,
    registered_client_id varchar(100) NOT NULL,
    principal_name varchar(200) NOT NULL,
    authorization_grant_type varchar(100) NOT NULL,
    authorized_scopes varchar(1000) DEFAULT NULL,
    attributes blob DEFAULT NULL,
    state varchar(500) DEFAULT NULL,
    authorization_code_value blob DEFAULT NULL,
    authorization_code_issued_at timestamp DEFAULT NULL,
    authorization_code_expires_at timestamp DEFAULT NULL,
    authorization_code_metadata blob DEFAULT NULL,
    access_token_value blob DEFAULT NULL,
    access_token_issued_at timestamp DEFAULT NULL,
    access_token_expires_at timestamp DEFAULT NULL,
    access_token_metadata blob DEFAULT NULL,
    access_token_type varchar(100) DEFAULT NULL,
    access_token_scopes varchar(1000) DEFAULT NULL,
    oidc_id_token_value blob DEFAULT NULL,
    oidc_id_token_issued_at timestamp DEFAULT NULL,
    oidc_id_token_expires_at timestamp DEFAULT NULL,
    oidc_id_token_metadata blob DEFAULT NULL,
    refresh_token_value blob DEFAULT NULL,
    refresh_token_issued_at timestamp DEFAULT NULL,
    refresh_token_expires_at timestamp DEFAULT NULL,
    refresh_token_metadata blob DEFAULT NULL,
    user_code_value blob DEFAULT NULL,
    user_code_issued_at timestamp DEFAULT NULL,
    user_code_expires_at timestamp DEFAULT NULL,
    user_code_metadata blob DEFAULT NULL,
    device_code_value blob DEFAULT NULL,
    device_code_issued_at timestamp DEFAULT NULL,
    device_code_expires_at timestamp DEFAULT NULL,
    device_code_metadata blob DEFAULT NULL,
    PRIMARY KEY (id),
    KEY registered_client_id_index (registered_client_id),
    KEY principal_name_index (principal_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OAuth2 授权信息表';

-- 授权确认表 (用于授权码模式的用户同意)
CREATE TABLE IF NOT EXISTS oauth2_authorization_consent (
    registered_client_id varchar(100) NOT NULL,
    principal_name varchar(200) NOT NULL,
    authorities varchar(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OAuth2 授权确认表';

-- ========================================================
-- RBAC 权限模型表
-- ========================================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id bigint NOT NULL AUTO_INCREMENT,
    username varchar(50) NOT NULL COMMENT '用户名',
    password varchar(200) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname varchar(50) DEFAULT NULL COMMENT '昵称',
    avatar varchar(200) DEFAULT NULL COMMENT '头像URL',
    email varchar(100) DEFAULT NULL COMMENT '邮箱',
    phone varchar(20) DEFAULT NULL COMMENT '手机号',
    enabled tinyint(1) DEFAULT 1 COMMENT '是否启用: 1-启用, 0-禁用',
    account_non_expired tinyint(1) DEFAULT 1 COMMENT '账户是否过期',
    account_non_locked tinyint(1) DEFAULT 1 COMMENT '账户是否锁定',
    credentials_non_expired tinyint(1) DEFAULT 1 COMMENT '凭证是否过期',
    created_at timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id bigint NOT NULL AUTO_INCREMENT,
    role_code varchar(50) NOT NULL COMMENT '角色编码(如: ADMIN, USER)',
    role_name varchar(50) NOT NULL COMMENT '角色名称',
    description varchar(200) DEFAULT NULL COMMENT '角色描述',
    enabled tinyint(1) DEFAULT 1 COMMENT '是否启用',
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code),
    KEY idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id bigint NOT NULL AUTO_INCREMENT,
    permission_code varchar(100) NOT NULL COMMENT '权限编码(如: user:read, order:write)',
    permission_name varchar(100) NOT NULL COMMENT '权限名称',
    resource_type varchar(50) DEFAULT NULL COMMENT '资源类型(menu, button, api)',
    parent_id bigint DEFAULT NULL COMMENT '父权限ID',
    sort_order int DEFAULT 0 COMMENT '排序',
    description varchar(200) DEFAULT NULL COMMENT '描述',
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (permission_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id bigint NOT NULL AUTO_INCREMENT,
    user_id bigint NOT NULL COMMENT '用户ID',
    role_id bigint NOT NULL COMMENT '角色ID',
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id bigint NOT NULL AUTO_INCREMENT,
    role_id bigint NOT NULL COMMENT '角色ID',
    permission_id bigint NOT NULL COMMENT '权限ID',
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    KEY idx_role_id (role_id),
    KEY idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 审计日志表
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id bigint NOT NULL AUTO_INCREMENT,
    operation_type varchar(50) NOT NULL COMMENT '操作类型(LOGIN, LOGOUT, TOKEN_ISSUE, TOKEN_REVOKE)',
    username varchar(50) DEFAULT NULL COMMENT '用户名',
    client_id varchar(100) DEFAULT NULL COMMENT '客户端ID',
    ip_address varchar(50) DEFAULT NULL COMMENT 'IP地址',
    user_agent varchar(500) DEFAULT NULL COMMENT '用户代理',
    request_uri varchar(500) DEFAULT NULL COMMENT '请求URI',
    request_method varchar(10) DEFAULT NULL COMMENT '请求方法',
    request_params text DEFAULT NULL COMMENT '请求参数',
    response_status int DEFAULT NULL COMMENT '响应状态码',
    error_message varchar(500) DEFAULT NULL COMMENT '错误信息',
    execution_time bigint DEFAULT NULL COMMENT '执行时间(ms)',
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_operation_type (operation_type),
    KEY idx_username (username),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- Token黑名单表 (用于存储已撤销的Token)
CREATE TABLE IF NOT EXISTS sys_token_blacklist (
    id bigint NOT NULL AUTO_INCREMENT,
    token_id varchar(100) NOT NULL COMMENT 'Token JTI (JWT ID)',
    token_type varchar(20) NOT NULL COMMENT 'Token类型(access_token, refresh_token)',
    username varchar(50) DEFAULT NULL COMMENT '用户名',
    client_id varchar(100) DEFAULT NULL COMMENT '客户端ID',
    expiration_time timestamp NOT NULL COMMENT 'Token过期时间',
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_token_id (token_id),
    KEY idx_expiration_time (expiration_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Token黑名单表';
