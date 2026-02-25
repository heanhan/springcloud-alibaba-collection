package com.jhzhao.alibaba.mcp.demo.config;

import com.jhzhao.alibaba.mcp.demo.component.MilkTeaOrderTool;
import com.jhzhao.alibaba.mcp.demo.component.WeatherTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2026/2/24 23:00
 * Version 1.0
 */
@Component
public class InitBeanConfig {

    //天气工具注册bean
    @Bean
    public ToolCallbackProvider weatherTools(WeatherTool weatherTool) {
        return MethodToolCallbackProvider.builder().toolObjects(weatherTool).build();
    }

    //奶茶工具注册bean
    @Bean
    public ToolCallbackProvider milkteaTools(MilkTeaOrderTool milkTeaOrderTool) {
        return MethodToolCallbackProvider.builder().toolObjects(milkTeaOrderTool).build();
    }
}
