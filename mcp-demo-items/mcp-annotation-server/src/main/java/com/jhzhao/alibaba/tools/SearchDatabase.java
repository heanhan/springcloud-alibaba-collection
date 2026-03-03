package com.jhzhao.alibaba.tools;

import com.alibaba.fastjson.JSONObject;
import com.jhzhao.alibaba.entity.Commodity;
import com.jhzhao.alibaba.entity.CommodityOrder;
import com.jhzhao.alibaba.service.CommodityOrderService;
import com.jhzhao.alibaba.service.CommodityService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
public class SearchDatabase {

    @Resource
    private CommodityService commodityService;

    @Resource
    private CommodityOrderService commodityOrderService;

    /**
     * 统一 MCP 工具：mcp_milk_tee_menu
     *
     * @param id       商品ID（仅 order 时需要）
     * @param quantity 数量（仅 order 时需要）
     * @return 结果描述
     */
    @McpTool(name = "mcp_milk_tee_menu", description = "查询奶茶菜单")
    public String mcpMilkTeaMenu(Integer id, Integer quantity) {
        log.info("调用 mcp_milk_tea 工具: id={}, quantity={}",id, quantity);
        List<Commodity> allCommodityList = commodityService.findAllCommodityList(null);
        String jsonString = JSONObject.toJSONString(allCommodityList);
        return "当前菜单:\n" + jsonString;
    }

    /**
     * 统一 MCP 工具：mcp_milk_tea_order
     *
     * @param name       商品ID（仅 order 时需要）
     * @param quantity 数量（仅 order 时需要）
     * @return 结果描述
     */
    @McpTool(name = "mcp_milk_tea_order", description = "奶茶下单购买。下单时需提供 name 和 quantity。")
    public String mcpMilkTeaOrder(String name, Integer quantity) {
        log.info("调用 mcp_milk_tea 工具: 奶茶名称:{}, 杯数：{}", name, quantity);
        List<Commodity> allCommodityList = commodityService.findAllCommodityList(null);
        if (name == null || name.isEmpty() || quantity == null) {
            return "下单失败：缺少商品ID或数量";
        }
        // 查找商品
        Commodity commodity = allCommodityList.stream()
                .filter(m -> m.getComodityName().equals(name))
                .findFirst()
                .orElse(null);

        if (commodity == null) {
            return "无效的商品明湖城那个: " + name;
        }
        try {
            double total = commodity.getPrice() * quantity;
            String result = String.format("下单成功：%s x %d，总价：%.2f 元", commodity.getComodityName(), quantity, total);
            log.info("订单完成: {}", result);
            CommodityOrder order = new CommodityOrder();
            order.setComodityCode(commodity.getComodityCode());
            order.setComodityName(commodity.getComodityName());
            order.setPrice(commodity.getPrice());
            order.setCount(quantity);
            order.setTotalPrice(total);
            order.setCreateTime(new Date());
            order.setUpdateTime(new Date());
            commodityOrderService.saveCommodityOrder(order);
            return result;
        } catch (NumberFormatException e) {
            return "数量必须是数字";
        }
    }

    @McpTool(name = "longRunningTask", description = "执行长时间运行的任务")
    public CompletableFuture<String> longRunningTask(@McpToolParam(description = "Task id") String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            // 模拟长时间任务
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Task " + taskId + " completed";
        });
    }

    @McpTool(name = "riskyOperation", description = "执行危险操作")
    public String riskyOperation(@McpToolParam(description = "Operation parameter") String param) {
        try {
            log.info("接收到的运行参数：{}", param);
            // 执行操作
            return "操作成功";
        } catch (Exception e) {
            log.error("操作失败", e);
            return "操作失败: " + e.getMessage();
        }
    }


}
