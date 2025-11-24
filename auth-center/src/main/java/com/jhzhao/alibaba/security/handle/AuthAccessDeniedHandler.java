package com.jhzhao.alibaba.security.handle;

import com.alibaba.fastjson.JSONObject;
import com.jhzhao.alibaba.enums.CommonEnum;
import com.jhzhao.alibaba.result.ResultBody;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 用户登录后，权限不足的 处理器
 */
@Component
public class AuthAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        ResultBody resultBody = ResultBody.error(CommonEnum.FORBIDDEN);
        response.getWriter().write(JSONObject.toJSONString(resultBody));
    }
}