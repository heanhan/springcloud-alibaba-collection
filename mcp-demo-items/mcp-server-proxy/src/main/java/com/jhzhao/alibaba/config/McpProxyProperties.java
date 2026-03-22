package com.jhzhao.alibaba.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2026/3/14 14:46
 * Version 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "mcp-proxy")
public class McpProxyProperties {
    private Map<String, ServerConfig> servers;
    private String defaultNamespace = "default";
    private String toolNameHeader = "X-Tool-Name";
    private String toolNameQueryParam = "tool";

    @Data
    public static class ServerConfig {
        private String url;
        private int timeoutMs = 30000;
        private int connectTimeoutMs = 5000;
    }
}
