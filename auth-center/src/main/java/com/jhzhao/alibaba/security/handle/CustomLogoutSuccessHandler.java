package com.jhzhao.alibaba.security.handle;

import com.alibaba.fastjson.JSONObject;
import com.jhzhao.alibaba.enums.CommonEnum;
import com.jhzhao.alibaba.result.ResultBody;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ResultBody resultBody = ResultBody.success(CommonEnum.LOGOUT_SUCCESS);
        response.getWriter().write(JSONObject.toJSONString(resultBody));
    }
}
