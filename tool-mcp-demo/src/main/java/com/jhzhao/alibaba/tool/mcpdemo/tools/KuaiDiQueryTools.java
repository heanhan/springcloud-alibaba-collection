package com.jhzhao.alibaba.tool.mcpdemo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2026/2/11 09:08
 * Version 1.0
 */

@Service
public class KuaiDiQueryTools {

    @Tool(description = "Please help me check the logistics information for the logistics tracking number ")
    public String getKuaiDiQuery(@ToolParam(description = "logistics tracking number,such as YT196807550996") String logisticsTrackingNumber) {
        return "已发货\n" +
                "09-11 14:09正在安排圆通快递揽收\n" +
                "仓库处理中\n" +
                "09-11 14:02已打印快递单\n" +
                "09-11 13:57已出库\n" +
                "商家备货中\n" +
                "09-11 13:23已下单，商家正在安排配货";
    }
}
