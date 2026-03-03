package com.jhzhao.alibaba.tools;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

@Service
public class WeatherTool {
    @McpTool(name = "getWeather", description = "Get current weather for a city")
    public String getWeather(@McpToolParam(description = "City name", required = true) String city) {
        // 实现天气查询逻辑
        return "The weather in " + city + " is sunny, 25°C";
    }
}

