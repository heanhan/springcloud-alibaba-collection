package com.jhzhao.alibaba.service;

import com.jhzhao.alibaba.service.impl.LoginResponse;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2025/11/15 13:32
 * Version 1.0
 */
public interface AuthService {
    LoginResponse login(String username, String password);
}