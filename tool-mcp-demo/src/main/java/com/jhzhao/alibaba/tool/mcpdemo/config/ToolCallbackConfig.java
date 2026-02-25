package com.jhzhao.alibaba.tool.mcpdemo.config;

import com.jhzhao.alibaba.tool.mcpdemo.tools.BaiduMapTools;
import com.jhzhao.alibaba.tool.mcpdemo.tools.KuaiDiQueryTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2026/2/11 09:10
 * Version 1.0
 */

@Configuration
public class ToolCallbackConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(BaiduMapTools baiduMapTools, KuaiDiQueryTools kuaiDiQueryTools) {
        return MethodToolCallbackProvider.builder().toolObjects(baiduMapTools,kuaiDiQueryTools).build();
    }
}
