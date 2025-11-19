package com.jhzhao.alibaba.security.handle;

import com.alibaba.fastjson.JSONObject;
import com.jhzhao.alibaba.enums.CommonEnum;
import com.jhzhao.alibaba.result.ResultBody;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ResultBody resultBody = ResultBody.error(CommonEnum.FORBIDDEN);
        response.getWriter().write(JSONObject.toJSONString(resultBody));
    }
}