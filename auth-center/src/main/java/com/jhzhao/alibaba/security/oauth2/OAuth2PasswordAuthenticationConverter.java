package com.jhzhao.alibaba.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * OAuth 2.0 Resource Owner Password Credentials Grant 转换器
 * 从 HTTP 请求中提取用户名和密码
 */
public class OAuth2PasswordAuthenticationConverter implements AuthenticationConverter {

    @Override
    @Nullable
    public Authentication convert(HttpServletRequest request) {
        // grant_type 必须是 password
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!AuthorizationGrantType.PASSWORD.getValue().equals(grantType)) {
            return null;
        }

        // 获取客户端认证信息
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        // 提取参数
        MultiValueMap<String, String> parameters = getParameters(request);

        // 用户名 (必需)
        String username = parameters.getFirst(OAuth2ParameterNames.USERNAME);
        if (!StringUtils.hasText(username) || parameters.get(OAuth2ParameterNames.USERNAME).size() != 1) {
            return null;
        }

        // 密码 (必需)
        String password = parameters.getFirst(OAuth2ParameterNames.PASSWORD);
        if (!StringUtils.hasText(password) || parameters.get(OAuth2ParameterNames.PASSWORD).size() != 1) {
            return null;
        }

        // scope (可选)
        String scope = parameters.getFirst(OAuth2ParameterNames.SCOPE);
        Set<String> scopes = null;
        if (StringUtils.hasText(scope)) {
            scopes = new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
        }

        // 附加参数
        Map<String, Object> additionalParameters = new HashMap<>();
        parameters.forEach((key, values) -> {
            if (!key.equals(OAuth2ParameterNames.GRANT_TYPE) &&
                !key.equals(OAuth2ParameterNames.USERNAME) &&
                !key.equals(OAuth2ParameterNames.PASSWORD) &&
                !key.equals(OAuth2ParameterNames.SCOPE)) {
                additionalParameters.put(key, values.size() == 1 ? values.get(0) : values);
            }
        });

        return new OAuth2PasswordAuthenticationToken(username, password, clientPrincipal, scopes);
    }

    private MultiValueMap<String, String> getParameters(HttpServletRequest request) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        request.getParameterMap().forEach((key, values) -> {
            for (String value : values) {
                parameters.add(key, value);
            }
        });
        return parameters;
    }
}
