package com.jhzhao.alibaba.config;

import com.jhzhao.alibaba.security.CustomJwtTokenCustomizer;
import com.jhzhao.alibaba.security.oauth2.OAuth2PasswordAuthenticationConverter;
import com.jhzhao.alibaba.security.oauth2.OAuth2PasswordAuthenticationProvider;
import com.jhzhao.alibaba.service.audit.AuditLogService;
import com.jhzhao.alibaba.service.user.CustomUserDetailsService;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import javax.sql.DataSource;

/**
 * OAuth2 Authorization Server 配置
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class AuthorizationServerConfig {

    private final JWKSource<SecurityContext> jwkSource;
    private final DataSource dataSource;
    private final CustomUserDetailsService userDetailsService;
    private final AuditLogService auditLogService;

    /**
     * Authorization Server 的 SecurityFilterChain
     * 优先级最高，处理所有 OAuth2 端点
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                // 启用 OIDC
                .oidc(Customizer.withDefaults())
                // 自定义 Token 生成器
                .tokenGenerator(tokenGenerator())
                // 配置 Token 端点 - 添加 Password Grant 支持
                .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                        .accessTokenRequestConverters(converters -> converters.add(new OAuth2PasswordAuthenticationConverter()))
                        .authenticationProviders(providers -> providers.add(
                                new OAuth2PasswordAuthenticationProvider(
                                        registeredClientRepository(),
                                        authorizationService(),
                                        tokenGenerator(),
                                        userDetailsService,
                                        passwordEncoder(),
                                        auditLogService
                                )
                        ))
                );

        http
                // 将需要认证的请求重定向到登录页
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                // 接受 JWT 格式的访问令牌
                .oauth2ResourceServer(resourceServer ->
                        resourceServer.jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * 默认的 SecurityFilterChain
     * 处理其他所有请求
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        // 公开端点
                        .requestMatchers(
                                "/login",
                                "/error",
                                "/actuator/**",
                                "/.well-known/**",
                                "/oauth2/jwks",
                                "/api/auth/**"
                        ).permitAll()
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )
                // 表单登录
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", false)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                // 登出
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * RegisteredClientRepository - 客户端注册信息存储
     * 使用 JDBC 存储到数据库
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        return new JdbcRegisteredClientRepository(new JdbcTemplate(dataSource));
    }

    /**
     * OAuth2AuthorizationService - 授权信息存储
     * 使用 JDBC 存储到数据库
     */
    @Bean
    public OAuth2AuthorizationService authorizationService() {
        return new JdbcOAuth2AuthorizationService(new JdbcTemplate(dataSource), registeredClientRepository());
    }

    /**
     * OAuth2AuthorizationConsentService - 授权确认信息存储
     * 使用 JDBC 存储到数据库
     */
    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService() {
        return new JdbcOAuth2AuthorizationConsentService(new JdbcTemplate(dataSource), registeredClientRepository());
    }

    /**
     * AuthorizationServerSettings - 授权服务器设置
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:8083")  // 认证服务器地址
                .authorizationEndpoint("/oauth2/authorize")
                .tokenEndpoint("/oauth2/token")
                .tokenIntrospectionEndpoint("/oauth2/introspect")
                .tokenRevocationEndpoint("/oauth2/revoke")
                .jwkSetEndpoint("/oauth2/jwks")
                .oidcLogoutEndpoint("/connect/logout")
                .oidcUserInfoEndpoint("/userinfo")
                .build();
    }

    /**
     * JwtDecoder - JWT 解码器
     * 用于验证 JWT Token
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * JwtEncoder - JWT 编码器
     * 用于生成 JWT Token
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * Token 生成器
     * 配置 JWT 格式 Token 和自定义 Claims
     */
    @Bean
    public OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator() {
        // JWT Encoder
        NimbusJwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);

        // JWT Token 定制器 - 注入 RBAC Claims
        OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer = new CustomJwtTokenCustomizer();

        // JWT 生成器
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(jwtCustomizer);

        // Access Token 生成器
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();

        // Refresh Token 生成器
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();

        // 组合生成器
        return new DelegatingOAuth2TokenGenerator(
                jwtGenerator,
                accessTokenGenerator,
                refreshTokenGenerator
        );
    }


}
