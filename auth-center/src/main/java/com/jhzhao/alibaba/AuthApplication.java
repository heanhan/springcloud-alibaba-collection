package com.jhzhao.alibaba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * auth-center 的权限认证中心的启动类
 *
 */
//@EnableDiscoveryClient
@SpringBootApplication
public class AuthApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(AuthApplication.class,args);
    }
}
