# OAuth 2.1 Authorization Server - Auth Center

## 项目简介

基于 Spring Authorization Server 1.5.0 构建的 OAuth 2.1 认证授权服务，支持 RBAC 权限模型、双 Token 机制、Redis 黑名单等功能。

## 技术栈

- Java 25
- Spring Boot 3.5.10
- Spring Authorization Server 1.5.0
- Spring Security 6.x
- MySQL 8.x
- Redis
- JWT (RSA-256)

## 快速开始

### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS auth_center_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 执行表结构脚本
mysql -u root -p auth_center_db < src/main/resources/db/schema-oauth2.sql

# 插入演示数据
mysql -u root -p auth_center_db < src/main/resources/db/data-oauth2.sql
```

### 2. 配置 Redis

确保 Redis 服务已启动，默认配置：
- Host: 172.16.75.105
- Port: 6379
- Password: abcd@123456

### 3. 启动服务

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home
mvn spring-boot:run
```

服务启动后访问：http://localhost:8083

## 支持的 OAuth 2.1 授权模式

### 1. Authorization Code + PKCE (外部客户端)

适用于 Web 应用、移动应用等公开客户端。

**步骤 1: 获取授权码**

```http
GET /oauth2/authorize?response_type=code
    &client_id=web-app-client
    &redirect_uri=http://localhost:3000/callback
    &scope=openid profile email read
    &state=xyz123
    &code_challenge=BASE64URL(SHA256(code_verifier))
    &code_challenge_method=S256
```

**步骤 2: 交换 Token**

```http
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
&code=AUTHORIZATION_CODE
&redirect_uri=http://localhost:3000/callback
&client_id=web-app-client
&code_verifier=CODE_VERIFIER
```

### 2. Client Credentials (内部客户端)

适用于微服务间的 Machine-to-Machine 通信。

```http
POST /oauth2/token
Authorization: Basic b3JkZXItc2VydmljZTpvcmRlci1zZXJ2aWNlLXNlY3JldA==
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&scope=order:read order:write
```

### 3. Refresh Token

刷新 Access Token：

```http
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token
&refresh_token=REFRESH_TOKEN
&client_id=web-app-client
```

### 4. Token 撤销

```http
POST /oauth2/revoke
Authorization: Bearer ACCESS_TOKEN
Content-Type: application/x-www-form-urlencoded

token=ACCESS_TOKEN_OR_REFRESH_TOKEN
&token_type_hint=access_token
```

## 客户端配置

### 外部客户端 (Web/Mobile)

| 客户端ID | 认证方式 | 授权模式 | PKCE |
|---------|---------|---------|------|
| web-app-client | none | authorization_code, refresh_token | 必需 |
| mobile-app-client | none | authorization_code, refresh_token | 必需 |

### 内部客户端 (微服务)

| 客户端ID | 认证方式 | 授权模式 | Client Secret |
|---------|---------|---------|---------------|
| order-service | client_secret_basic | client_credentials, refresh_token | order-service-secret |
| user-service | client_secret_basic | client_credentials, refresh_token | user-service-secret |
| payment-service | client_secret_basic | client_credentials, refresh_token | payment-service-secret |

### 测试客户端

| 客户端ID | 认证方式 | 授权模式 | Client Secret |
|---------|---------|---------|---------------|
| test-client | client_secret_basic, none | authorization_code, client_credentials, refresh_token | test-secret |

## 演示用户

| 用户名 | 密码 | 角色 |
|-------|------|------|
| admin | password123 | SUPER_ADMIN |
| zhangsan | password123 | ADMIN |
| lisi | password123 | USER |
| wangwu | password123 | ORDER_MANAGER, USER |

## API 端点

### OAuth 2.0 端点

| 端点 | 说明 |
|------|------|
| GET /oauth2/authorize | 授权端点 |
| POST /oauth2/token | Token 端点 |
| POST /oauth2/revoke | Token 撤销端点 |
| GET /oauth2/jwks | JWK Set 端点 |
| POST /oauth2/introspect | Token 自省端点 |

### OIDC 端点

| 端点 | 说明 |
|------|------|
| GET /userinfo | 用户信息端点 |
| GET /.well-known/openid-configuration | OIDC 配置 |

### 用户 API

| 端点 | 说明 |
|------|------|
| GET /api/user/me | 获取当前用户信息 |

### 监控端点

| 端点 | 说明 |
|------|------|
| GET /actuator/health | 健康检查 |
| GET /actuator/metrics | 指标数据 |
| GET /actuator/prometheus | Prometheus 格式指标 |

## JWT Token 结构

### Access Token Claims

```json
{
  "sub": "admin",
  "aud": "web-app-client",
  "nbf": 1700000000,
  "scope": "openid profile email",
  "roles": ["SUPER_ADMIN"],
  "permissions": ["user:read", "user:write", "system:admin"],
  "user_id": 1,
  "nickname": "系统管理员",
  "email": "admin@example.com",
  "client_id": "web-app-client",
  "grant_type": "authorization_code",
  "jti": "uuid-token-id",
  "iat": 1700000000,
  "exp": 1700001800
}
```

## 项目结构

```
auth-center/
├── src/main/java/com/jhzhao/alibaba/
│   ├── AuthApplication.java          # 启动类
│   ├── config/                       # 配置类
│   │   ├── AuthorizationServerConfig.java  # OAuth2 授权服务器配置
│   │   ├── JwkConfig.java            # JWT 密钥配置
│   │   ├── RedisConfig.java          # Redis 配置
│   │   └── AsyncConfig.java          # 异步任务配置
│   ├── controller/                   # 控制器
│   │   ├── TokenRevocationController.java  # Token 撤销
│   │   └── UserInfoController.java   # 用户信息
│   ├── entity/                       # 实体类
│   │   ├── user/User.java            # 用户实体
│   │   ├── rbac/                     # RBAC 实体
│   │   │   ├── Role.java
│   │   │   ├── Permission.java
│   │   │   ├── UserRole.java
│   │   │   └── RolePermission.java
│   │   ├── oauth2/                   # OAuth2 实体
│   │   │   └── TokenBlacklist.java
│   │   └── audit/                    # 审计实体
│   │       └── AuditLog.java
│   ├── repository/                   # 数据访问层
│   │   ├── user/
│   │   ├── rbac/
│   │   ├── oauth2/
│   │   └── audit/
│   ├── security/                     # 安全相关
│   │   └── CustomJwtTokenCustomizer.java  # JWT 定制器
│   └── service/                      # 业务层
│       ├── user/
│       │   └── CustomUserDetailsService.java
│       ├── oauth2/
│       │   └── TokenBlacklistService.java
│       └── audit/
│           └── AuditLogService.java
├── src/main/resources/
│   ├── application.yml               # 应用配置
│   └── db/
│       ├── schema-oauth2.sql         # 数据库表结构
│       └── data-oauth2.sql           # 演示数据
└── pom.xml
```

## 安全特性

1. **Token 黑名单**: 使用 Redis + MySQL 双存储，支持 Token 撤销
2. **密钥管理**: RSA-2048 密钥对，支持密钥轮换
3. **审计日志**: 记录所有认证授权事件
4. **RBAC 权限**: 细粒度权限控制
5. **PKCE**: 保护公开客户端的授权码流程

## 扩展性

- 支持 Redis Sentinel/Cluster 模式
- 支持多租户 (需扩展)
- 支持自定义 Token 格式
- 支持多种客户端认证方式
