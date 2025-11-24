package com.jhzhao.alibaba.config;

import com.jhzhao.alibaba.entity.SysUser;
import com.jhzhao.alibaba.security.AuthenticationTokenFilter;
import com.jhzhao.alibaba.security.UserDetailsServiceImpl;
import com.jhzhao.alibaba.security.handle.AuthAccessDeniedHandler;
import com.jhzhao.alibaba.security.handle.AuthLoginSuccessHandler;
import com.jhzhao.alibaba.security.handle.NonAuthenticationEntryPoint;
import com.jhzhao.alibaba.security.handle.TokenClearLogoutHandler;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.AntPathMatcher;

/**
 *  spring security 的核心处理类
 */

//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity
//public class SecurityConfig {
//
//    @Resource
//    private UserDetailsServiceImpl userService;
//
//    @Resource
//    private AuthAccessDeniedHandler accessDeniedHandler;
//
//    @Resource
//    private NonAuthenticationEntryPoint nonAuthenticationEntryPoint;
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/auth/login", "/api/auth/logout", "/api/auth/refresh").permitAll() //对一些接口进行白名单放行
//                        .anyRequest().authenticated()//除了白名单 所有的路径全部需要通过认证才能放行
//                )
//                //使用JWT的验证器
//                .addFilterBefore(authenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class)
//                .exceptionHandling(ex -> ex
//                        .accessDeniedHandler(accessDeniedHandler)//处理未授权的
//                        .authenticationEntryPoint(nonAuthenticationEntryPoint)// 处理未登录
//                );
//        return http.build();
//    }
//
//    //认证管理器
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//
//    //密码管理器
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    /**
//     * 通过基于 关系型数据进行权限认证处理
//     * @return
//     */
//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(userService);
//        provider.setPasswordEncoder(passwordEncoder());
//        return provider;
//    }
//
//    //对每一次请求的拦截
//    @Bean
//    public AuthenticationTokenFilter authenticationTokenFilter() {
//        return new AuthenticationTokenFilter();
//    }
//}


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    @Resource
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/auth/login", "/auth/logout", "/auth/refresh","/auth/register").permitAll()
                        .anyRequest().access(customAuthorizationManager())
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new NonAuthenticationEntryPoint())
                        .accessDeniedHandler(new AuthAccessDeniedHandler())
                )
                .addFilterBefore(new AuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(new TokenClearLogoutHandler())
                        .logoutSuccessHandler(new AuthLoginSuccessHandler())
                        .permitAll()
                );

        return http.build();
    }

    private AuthorizationManager<RequestAuthorizationContext> customAuthorizationManager() {
        return (supplier, ctx) -> {
            HttpServletRequest req = ctx.getRequest();
            String uri = req.getRequestURI();
            Authentication auth = supplier.get();
            if (!(auth instanceof UsernamePasswordAuthenticationToken)) {
                return new AuthorizationDecision(false);
            }
            SysUser user = (SysUser) auth.getPrincipal();
            boolean granted = user.getPermissions().stream()
                    .anyMatch(p -> new AntPathMatcher().match(p, uri));
            return new AuthorizationDecision(granted);
        };
    }

}