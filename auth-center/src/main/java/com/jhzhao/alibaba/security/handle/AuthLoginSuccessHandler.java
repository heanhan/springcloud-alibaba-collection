package com.jhzhao.alibaba.security.handle;

import com.alibaba.fastjson.JSONObject;
import com.jhzhao.alibaba.enums.CommonEnum;
import com.jhzhao.alibaba.result.ResultBody;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 用户登录成功  处理器
 */

@Component
public class AuthLoginSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        ResultBody resultBody = ResultBody.success(CommonEnum.LOGOUT_SUCCESS);
        response.getWriter().write(JSONObject.toJSONString(resultBody));
    }
}
