package com.jhzhao.alibaba.mcp.demo;

import com.jhzhao.alibaba.mcp.demo.service.MilkTeaOrderService;
import com.jhzhao.alibaba.mcp.demo.service.WeatherService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

/**
 * mcp server 的测试demo
 *
 */

@SpringBootApplication
@EnableDiscoveryClient
public class McpServerApplication
{
    public static void main( String[] args ){
        System.out.println( "Hello World!" );
        SpringApplication.run(McpServerApplication.class);
    }

    //天气工具注册bean
    @Bean
    public ToolCallbackProvider weatherTools(WeatherService weatherService) {
        return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
    }

    //奶茶工具注册bean
    @Bean
    public ToolCallbackProvider milkteaTools(MilkTeaOrderService milkTeaOrderService) {
        return MethodToolCallbackProvider.builder().toolObjects(milkTeaOrderService).build();
    }
}
