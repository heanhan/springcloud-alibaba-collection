package com.jhzhao.alibaba.mcp.demo.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2026/2/24 22:56
 * Version 1.0
 */
@Slf4j
@Component
public class WeatherTool {

    @Tool(description = "通过城市名称获取天气信息")
    public String getWeather(@ToolParam(description = "城市名称") String cityName) {
        return "Sunny in " + cityName;
    }
}
