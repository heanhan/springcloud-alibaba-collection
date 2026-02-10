package com.jhzhao.alibaba.mcp.demo.interfaces;

import java.util.Map;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2026/2/10 16:50
 * Version 1.0
 */
public interface ToolInterface {
    String getName();
    String getDescription();
    Object getInputSchema(); // 返回 JSON Schema
    Object invoke(Map<String, Object> input);
}
