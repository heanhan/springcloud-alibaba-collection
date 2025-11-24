package com.jhzhao.alibaba.config;

import com.jhzhao.alibaba.entity.SysUser;
import com.jhzhao.alibaba.security.AuthenticationTokenFilter;
import com.jhzhao.alibaba.security.handle.AuthAccessDeniedHandler;
import com.jhzhao.alibaba.security.handle.AuthLoginSuccessHandler;
import com.jhzhao.alibaba.security.handle.NonAuthenticationEntryPoint;
import com.jhzhao.alibaba.security.handle.TokenClearLogoutHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.AntPathMatcher;

/**
 *  spring security 的核心处理类
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);
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