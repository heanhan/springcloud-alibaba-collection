package com.jhzhao.alibaba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OAuth 2.1 Authorization Server 启动类
 *
 * @author jhzhao
 */
@SpringBootApplication(scanBasePackages = "com.jhzhao.alibaba")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
