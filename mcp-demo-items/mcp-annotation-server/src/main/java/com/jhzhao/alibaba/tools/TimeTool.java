package com.jhzhao.alibaba.tools;

import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class TimeTool {


    @McpTool(name = "getCityTime", description = "获取指定城市的时间。")
    public String getCityTimeMethod(@McpToolParam(description = "时区 ID, 例如 Asia/Shanghai", required = true) String timeZoneId) {
        log.info("进入mcp后端服务-getCityMethod");
        log.info("当前时区为:{}", timeZoneId);
        return String.format("The current time zone is %s and the current time is " + "%s", timeZoneId,
                getTimeByZoneId(timeZoneId));
    }


    private String getTimeByZoneId(String zoneId) {
        ZoneId zid = ZoneId.of(zoneId);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zid);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        return zonedDateTime.format(formatter);
    }
}
