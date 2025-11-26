package com.jhzhao.alibaba.security;

import com.jhzhao.alibaba.enums.CommonEnum;
import com.jhzhao.alibaba.exceptins.BizException;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class AuthenticationTokenFilter extends OncePerRequestFilter {

    @Resource
    private TokenService tokenService;

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

        }else{
            log.info("AuthenticationTokenFilter:黑名单功能未启用");
        }
        // 忽略的url列表
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        if(ignoreUrls.stream().anyMatch(url->antPathMatcher.match(url,request.getRequestURI()))){
            chain.doFilter(request,response);
            return;
        }
        //根据token 进行判断了
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        try{
            //如果token为空或者token不是以 Bearer开头的，则为匿名用户
            if(!StringUtils.hasText(token)||!token.startsWith("Bearer")){
                chain.doFilter(request,response);
                return;
            }
            token = token.substring(7);
            //解析token
            boolean validate = tokenService.validateAccessToken(token);
            if(validate){
                log.info("JwtValidationFilter error，token校验: {}",validate );
                SecurityContextHolder.clearContext();
                throw new BizException(CommonEnum.FORBIDDEN);
            }

        }catch (Exception e){
            log.error("JwtValidationFilter error: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            throw new BizException(CommonEnum.FORBIDDEN);
        }
        //token有效或者无token时继续执行过滤链
        chain.doFilter(request,response);
    }

}
