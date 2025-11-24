package com.jhzhao.alibaba.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class AuthenticationTokenFilter extends OncePerRequestFilter {

    // 忽略的URL列表
    private List<String> ignoreUrls;

    @Value("${black.list.enable:false}")
    public String blacklistEnabled;

    /**
     * 两个功能，一个是认证 一个是授权
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        //判断是否开启黑名单的的功能，且请求头中有Authorization 信息，则检查Token是否在黑名单中
        if ("true".equals(blacklistEnabled)) {
            log.info("AuthenticationTokenFilter：检查黑名单功能启动");
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            Assert.notNull(header, "请求头不包含 Authorization");
            if(header.startsWith("")){

            }

        }
        chain.doFilter(request,response);


    }
    // 放行

}
