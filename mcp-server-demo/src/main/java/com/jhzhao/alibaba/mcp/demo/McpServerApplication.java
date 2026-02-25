package com.jhzhao.alibaba.mcp.demo;

import com.jhzhao.alibaba.mcp.demo.component.MilkTeaOrderTool;
import com.jhzhao.alibaba.mcp.demo.component.WeatherTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

/**
 * mcp server 的测试demo
 *
 */

@SpringBootApplication
@EnableDiscoveryClient
public class McpServerApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(McpServerApplication.class)
                .web(WebApplicationType.REACTIVE) // ⚠️ 强制 Reactive 模式
                .run(args);
    }


}
