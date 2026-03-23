package com.jhzhao.alibaba.security.oauth2;

import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * OAuth 2.0 Resource Owner Password Credentials Grant 认证令牌
 */
public class OAuth2PasswordAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;
    private final Set<String> scopes;

    /**
     * 构造函数
     *
     * @param username      用户名
     * @param password      密码
     * @param clientPrincipal 客户端认证
     * @param scopes        请求的 scope
     */
    public OAuth2PasswordAuthenticationToken(
            String username,
            String password,
            Authentication clientPrincipal,
            @Nullable Set<String> scopes) {
        super(AuthorizationGrantType.PASSWORD, clientPrincipal, null);
        this.username = username;
        this.password = password;
        this.scopes = Collections.unmodifiableSet(scopes != null ? new HashSet<>(scopes) : Collections.emptySet());
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public Set<String> getScopes() {
        return this.scopes;
    }
}
