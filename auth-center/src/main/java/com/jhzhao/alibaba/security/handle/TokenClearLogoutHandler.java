package com.jhzhao.alibaba.security.handle;

import com.jhzhao.alibaba.security.TokenCache;
import com.jhzhao.alibaba.security.TokenService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TokenClearLogoutHandler implements LogoutHandler {

    @Resource
    private TokenCache tokenCache;

    @Resource
    private TokenService tokenService;

    @Override
    public void logout(HttpServletRequest req, HttpServletResponse resp, Authentication auth) {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            String username = tokenService.getUsernameFromToken(token);
            tokenCache.deleteTokens(username);
        }
    }
}
