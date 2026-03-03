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
     * 统一 MCP 工具：mcp_milk_tea
     *
     * @param action   操作类型：menu 或 order
     * @param id       商品ID（仅 order 时需要）
     * @param quantity 数量（仅 order 时需要）
     * @return 结果描述
     */
    @McpTool(name = "mcp_milk_tea",description = "查询奶茶菜单或下单购买。action 必填：'menu' 查菜单，'order' 下单；下单时需提供 id 和 quantity。")
    public String mcpMilkTea(String action, Integer id, Integer quantity) {
        log.info("调用 mcp_milk_tea 工具: action={}, id={}, quantity={}", action, id, quantity);
        List<Commodity> allCommodityList = commodityService.findAllCommodityList(null);
        String jsonString = JSONObject.toJSONString(allCommodityList);
        if ("menu".equals(action)) {
            // 返回菜单字符串（或 JSON，但 MCP 通常期望 string）

            return "当前菜单:\n" + jsonString;
        }

        if ("order".equals(action)) {
            if (id == null || quantity == null) {
                return "下单失败：缺少商品ID或数量";
            }

            // 查找商品
            Commodity commodity = allCommodityList.stream()
                    .filter(m -> m.getId()==id)
                    .findFirst()
                    .orElse(null);

            if (commodity == null) {
                return "无效的商品ID: " + id;
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

        return "不支持的操作类型，请使用 'menu' 或 'order'";
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
            log.info("接收到的运行参数：{}",param);
            // 执行操作
            return "操作成功";
        } catch (Exception e) {
            log.error("操作失败", e);
            return "操作失败: " + e.getMessage();
        }
    }


}
