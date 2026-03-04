package com.jhzhao.alibaba.handlers;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpLogging;
import org.springaicommunity.mcp.annotation.McpProgress;
import org.springaicommunity.mcp.annotation.McpSampling;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MyClientHandlers {

    // 处理日志消息
    @McpLogging(clients = "my-mcp-client")
    public void handleLogs(McpSchema.LoggingMessageNotification notification) {
        log.info("处理日志消息: {}", notification.data());
    }
    // 处理采样请求
    @McpSampling(clients = "my-mcp-client")
    public McpSchema.CreateMessageResult handleSampling(McpSchema.CreateMessageRequest request) {
        log.info("处理采样请求: {}", request.messages());
        return null;
    }
    // 处理进度通知
    @McpProgress(clients = "my-mcp-client")
    public void handleProgress(McpSchema.ProgressNotification notification) {
        log.info("处理进度通知: {}", notification);
    }


}

