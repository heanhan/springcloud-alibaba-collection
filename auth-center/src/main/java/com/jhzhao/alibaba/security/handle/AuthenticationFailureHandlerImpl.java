package com.jhzhao.alibaba.security.handle;

import com.alibaba.fastjson.JSONObject;
import com.jhzhao.alibaba.enums.CommonEnum;
import com.jhzhao.alibaba.result.ResultBody;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthenticationFailureHandlerImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ResultBody resultBody = ResultBody.error(CommonEnum.LOGIN_FAILED);
        response.getWriter().write(JSONObject.toJSONString(resultBody));
    }
}