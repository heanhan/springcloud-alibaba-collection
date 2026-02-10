package com.jhzhao.alibaba.mcp.demo.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2026/2/10 17:14
 * Version 1.0
 */
@Service
public class WeatherService {

    @Tool(description = "通过城市名称获取天气信息")
    public String getWeather(@ToolParam(description = "城市名称") String cityName) {
        return "Sunny in " + cityName;
    }
}
