package com.jhzhao.alibaba.mcp.demo.component;

import com.jhzhao.alibaba.mcp.demo.interfaces.ToolInterface;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2026/2/10 16:52
 * Version 1.0
 */
@Component
public class TimeTool implements ToolInterface {

    @Override
    public String getName() {
        return "获取当前时间";
    }

    @Override
    public String getDescription() {
        return "返回当前时间 ISO8601 格式.";
    }

    @Override
    public Object getInputSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of()
        );
    }

    @Override
    public Object invoke(Map<String, Object> input) {
        return Map.of(
                "time", Instant.now().toString(),
                "timezone", ZoneId.systemDefault()
        );
    }
}

