package com.jhzhao.alibaba.controller;

import com.jhzhao.alibaba.config.McpProxyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Optional;

/**
 * Author zhaojh0912
 * Description mcp server 的入口
 * CreateDate 2026/3/14 14:47
 * Version 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class McpProxyController {

    private final McpProxyProperties properties;
    private final WebClient webClient;

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> proxySse(
            @RequestParam(value = "tool", required = false) String toolFromQuery,
            @RequestHeader(value = "X-Tool-Name", required = false) String toolFromHeader) {

        // 优先级：header > query param
        String toolName = Optional.ofNullable(toolFromHeader)
                .filter(s -> !s.isBlank())
                .orElse(toolFromQuery);

        if (toolName == null || toolName.trim().isEmpty()) {
            log.warn("Missing tool name in request");
            return Flux.just(ServerSentEvent.builder("error: missing tool name").build());
        }
        // 确定目标 server
        String namespace = extractNamespace(toolName);
        McpProxyProperties.ServerConfig config = properties.getServers().getOrDefault(
                namespace,
                properties.getServers().getOrDefault(properties.getDefaultNamespace(), null)
        );

        if (config == null || config.getUrl() == null) {
            log.error("No target server found for namespace: {}", namespace);
            return Flux.just(ServerSentEvent.builder("error: no backend server configured").build());
        }

        log.info("Proxying tool '{}' to backend: {}", toolName, config.getUrl());

        return webClient.get()
                .uri(config.getUrl())
                // 可选：透传部分 header 或添加自定义 header
                //.header("X-Original-Tool", toolName)
                .retrieve()
                .bodyToFlux(String.class)
                .map(data -> ServerSentEvent.<String>builder()
                        .data(data)
                        .build())
                .onErrorResume(e -> {
                    log.error("SSE proxy error for {} : {}", config.getUrl(), e.getMessage(), e);
                    return Flux.just(ServerSentEvent.builder("error: " + e.getMessage()).build());
                })
                .take(Duration.ofMillis(config.getTimeoutMs()));
    }

    private String extractNamespace(String toolName) {
        if (toolName.contains("_")) {
            return toolName.substring(0, toolName.indexOf("_")).toLowerCase();
        }
        return properties.getDefaultNamespace();
    }
}