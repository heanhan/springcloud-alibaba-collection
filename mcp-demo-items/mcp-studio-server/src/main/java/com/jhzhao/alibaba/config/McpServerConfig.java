package com.jhzhao.alibaba.config;

import com.jhzhao.alibaba.service.CurrencyService;
import com.jhzhao.alibaba.service.OpenMeteoService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2026/3/4 22:14
 * Version 1.0
 */
@Configuration
public class McpServerConfig {


    /**
     * 注册工具提供者，将OpenMeteoService中的@Tool注解方法暴露为MCP工具
     */
    @Bean
    public ToolCallbackProvider allTools(OpenMeteoService penMeteoService, CurrencyService currencyService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(penMeteoService, currencyService)
                .build();
    }


}
