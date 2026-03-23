-- ========================================================
-- OAuth 2.1 Authorization Server 演示数据
-- ========================================================

-- ========================================================
-- 1. RBAC 基础数据
-- ========================================================

-- 插入权限数据
INSERT INTO sys_permission (permission_code, permission_name, resource_type, description) VALUES
('user:read', '用户查看', 'api', '查看用户列表和详情'),
('user:write', '用户编辑', 'api', '创建、修改、删除用户'),
('user:delete', '用户删除', 'api', '删除用户'),
('role:read', '角色查看', 'api', '查看角色列表和详情'),
('role:write', '角色编辑', 'api', '创建、修改、删除角色'),
('role:delete', '角色删除', 'api', '删除角色'),
('permission:read', '权限查看', 'api', '查看权限列表'),
('permission:write', '权限编辑', 'api', '创建、修改权限'),
('order:read', '订单查看', 'api', '查看订单列表和详情'),
('order:write', '订单编辑', 'api', '创建、修改订单'),
('order:delete', '订单删除', 'api', '删除订单'),
('system:admin', '系统管理', 'api', '系统管理权限'),
('profile:read', '个人信息查看', 'api', '查看个人信息'),
('profile:write', '个人信息编辑', 'api', '修改个人信息');

-- 插入角色数据
INSERT INTO sys_role (role_code, role_name, description) VALUES
('SUPER_ADMIN', '超级管理员', '拥有系统所有权限'),
('ADMIN', '管理员', '拥有大部分管理权限'),
('USER', '普通用户', '拥有基本操作权限'),
('ORDER_MANAGER', '订单管理员', '负责订单管理'),
('GUEST', '访客', '只读权限');

-- 角色权限关联
-- SUPER_ADMIN 拥有所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- ADMIN 拥有除删除外的所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission WHERE permission_code NOT LIKE '%:delete';

-- USER 拥有基本权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 3, id FROM sys_permission WHERE permission_code IN ('user:read', 'profile:read', 'profile:write', 'order:read');

-- ORDER_MANAGER 拥有订单相关权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 4, id FROM sys_permission WHERE permission_code LIKE 'order:%';

-- GUEST 只有查看权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 5, id FROM sys_permission WHERE permission_code LIKE '%:read';

-- 插入用户数据 (密码均为: password123，BCrypt加密)
-- $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO 是 password123 的加密结果
INSERT INTO sys_user (username, password, nickname, email, phone, enabled) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '系统管理员', 'admin@example.com', '13800138000', 1),
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '张三', 'zhangsan@example.com', '13800138001', 1),
('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '李四', 'lisi@example.com', '13800138002', 1),
('wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '王五', 'wangwu@example.com', '13800138003', 1),
('disabled_user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '禁用用户', 'disabled@example.com', '13800138004', 0);

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),  -- admin -> SUPER_ADMIN
(2, 2),  -- zhangsan -> ADMIN
(3, 3),  -- lisi -> USER
(4, 4),  -- wangwu -> ORDER_MANAGER
(4, 3);  -- wangwu -> USER (多角色)

-- ========================================================
-- 2. OAuth2 客户端注册数据
-- ========================================================

-- 外部客户端: Web应用 (Authorization Code + PKCE)
INSERT INTO oauth2_registered_client (
    id, client_id, client_id_issued_at, client_secret, client_secret_expires_at,
    client_name, client_authentication_methods, authorization_grant_types,
    redirect_uris, post_logout_redirect_uris, scopes, client_settings, token_settings
) VALUES (
    'web-client-001',
    'web-app-client',
    CURRENT_TIMESTAMP,
    NULL,  -- PKCE 不需要 client_secret
    NULL,
    'Web Application Client',
    'none',  -- PKCE 使用 none 认证方式
    'authorization_code,refresh_token',
    'http://localhost:3000/callback,http://localhost:8080/callback,http://127.0.0.1:3000/callback',
    'http://localhost:3000/logout,http://localhost:8080/logout',
    'openid,profile,email,read,write',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":true,"settings.client.require-authorization-consent":true}',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",1800],"settings.token.refresh-token-time-to-live":["java.time.Duration",604800],"settings.token.authorization-code-time-to-live":["java.time.Duration",300]}'
);

-- 外部客户端: 移动应用 (Authorization Code + PKCE)
INSERT INTO oauth2_registered_client (
    id, client_id, client_id_issued_at, client_secret, client_secret_expires_at,
    client_name, client_authentication_methods, authorization_grant_types,
    redirect_uris, post_logout_redirect_uris, scopes, client_settings, token_settings
) VALUES (
    'mobile-client-001',
    'mobile-app-client',
    CURRENT_TIMESTAMP,
    NULL,  -- PKCE 不需要 client_secret
    NULL,
    'Mobile Application Client',
    'none',
    'authorization_code,refresh_token',
    'com.example.app://callback,myapp://callback,http://localhost/callback',
    'com.example.app://logout',
    'openid,profile,email,read',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":true,"settings.client.require-authorization-consent":true}',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",1800],"settings.token.refresh-token-time-to-live":["java.time.Duration",604800],"settings.token.authorization-code-time-to-live":["java.time.Duration",300]}'
);

-- 内部客户端: 订单服务 (Client Credentials)
-- client_secret: order-service-secret (BCrypt加密)
INSERT INTO oauth2_registered_client (
    id, client_id, client_id_issued_at, client_secret, client_secret_expires_at,
    client_name, client_authentication_methods, authorization_grant_types,
    redirect_uris, post_logout_redirect_uris, scopes, client_settings, token_settings
) VALUES (
    'internal-client-001',
    'order-service',
    CURRENT_TIMESTAMP,
    '{bcrypt}$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO',  -- order-service-secret
    NULL,
    'Order Service (Internal)',
    'client_secret_basic,client_secret_post',
    'client_credentials,refresh_token',
    NULL,
    NULL,
    'order:read,order:write',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false}',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.access-token-time-to-live":["java.time.Duration",3600],"settings.token.refresh-token-time-to-live":["java.time.Duration",604800]}'
);

-- 内部客户端: 用户服务 (Client Credentials)
-- client_secret: user-service-secret (BCrypt加密)
INSERT INTO oauth2_registered_client (
    id, client_id, client_id_issued_at, client_secret, client_secret_expires_at,
    client_name, client_authentication_methods, authorization_grant_types,
    redirect_uris, post_logout_redirect_uris, scopes, client_settings, token_settings
) VALUES (
    'internal-client-002',
    'user-service',
    CURRENT_TIMESTAMP,
    '{bcrypt}$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO',  -- user-service-secret
    NULL,
    'User Service (Internal)',
    'client_secret_basic,client_secret_post',
    'client_credentials,refresh_token',
    NULL,
    NULL,
    'user:read,user:write',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false}',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.access-token-time-to-live":["java.time.Duration",3600],"settings.token.refresh-token-time-to-live":["java.time.Duration",604800]}'
);

-- 内部客户端: 支付服务 (Client Credentials)
-- client_secret: payment-service-secret (BCrypt加密)
INSERT INTO oauth2_registered_client (
    id, client_id, client_id_issued_at, client_secret, client_secret_expires_at,
    client_name, client_authentication_methods, authorization_grant_types,
    redirect_uris, post_logout_redirect_uris, scopes, client_settings, token_settings
) VALUES (
    'internal-client-003',
    'payment-service',
    CURRENT_TIMESTAMP,
    '{bcrypt}$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO',  -- payment-service-secret
    NULL,
    'Payment Service (Internal)',
    'client_secret_basic,client_secret_post',
    'client_credentials,refresh_token',
    NULL,
    NULL,
    'order:read,order:write',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false}',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.access-token-time-to-live":["java.time.Duration",1800],"settings.token.refresh-token-time-to-live":["java.time.Duration",604800]}'
);

-- 测试客户端: 支持所有授权类型 (用于开发和测试)
-- client_secret: test-secret (BCrypt加密)
INSERT INTO oauth2_registered_client (
    id, client_id, client_id_issued_at, client_secret, client_secret_expires_at,
    client_name, client_authentication_methods, authorization_grant_types,
    redirect_uris, post_logout_redirect_uris, scopes, client_settings, token_settings
) VALUES (
    'test-client-001',
    'test-client',
    CURRENT_TIMESTAMP,
    '{bcrypt}$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO',  -- test-secret
    NULL,
    'Test Client (Development)',
    'client_secret_basic,client_secret_post,none',
    'authorization_code,client_credentials,password,refresh_token',
    'http://localhost:8080/login/oauth2/code/test-client,http://localhost:3000/callback,http://127.0.0.1:8080/callback',
    'http://localhost:8080/logout',
    'openid,profile,email,read,write,admin',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false}',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",300],"settings.token.refresh-token-time-to-live":["java.time.Duration",3600],"settings.token.authorization-code-time-to-live":["java.time.Duration",120]}'
);

-- 内部系统客户端: 支持用户名密码登录 (Password Grant)
-- client_secret: internal-secret (BCrypt加密)
INSERT INTO oauth2_registered_client (
    id, client_id, client_id_issued_at, client_secret, client_secret_expires_at,
    client_name, client_authentication_methods, authorization_grant_types,
    redirect_uris, post_logout_redirect_uris, scopes, client_settings, token_settings
) VALUES (
    'internal-system-client-001',
    'internal-system',
    CURRENT_TIMESTAMP,
    '{bcrypt}$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO',  -- internal-secret
    NULL,
    'Internal System Client',
    'client_secret_basic,client_secret_post',
    'password,refresh_token',
    NULL,
    NULL,
    'openid,profile,email,read,write',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false}',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",1800],"settings.token.refresh-token-time-to-live":["java.time.Duration",604800]}'
);
