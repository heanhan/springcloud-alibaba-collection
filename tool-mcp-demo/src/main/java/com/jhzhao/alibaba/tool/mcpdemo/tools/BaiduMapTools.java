package com.jhzhao.alibaba.tool.mcpdemo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2026/2/11 09:09
 * Version 1.0
 */
@Service
public class BaiduMapTools {

    @Tool(description = "Help me plan the route from the departure point to the destination ")
    public String getLine(@ToolParam(description = "departure point,such as beijing") String departurePoint
            ,@ToolParam(description = "destination,such as shanghai") String destination){
        return "自驾出行";
    }
}