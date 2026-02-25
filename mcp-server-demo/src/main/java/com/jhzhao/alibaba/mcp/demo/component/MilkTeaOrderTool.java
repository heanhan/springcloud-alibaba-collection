package com.jhzhao.alibaba.mcp.demo.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2026/2/24 22:56
 * Version 1.0
 */

@Slf4j
@Component
public class MilkTeaOrderTool {

    // 菜单数据（保持不变）
    private final List<Map<String, Object>> menu = Arrays.asList(
            Map.of("id", "original_milk_tea", "name", "原味奶茶", "price", 5.0),
            Map.of("id", "green_milk_tea", "name", "绿奶茶", "price", 5.5),
            Map.of("id", "pearl_milk_tea", "name", "珍珠奶茶", "price", 6.0)
    );

    /**
     * 统一 MCP 工具：mcp_milk_tea
     *
     * @param action   操作类型：menu 或 order
     * @param id       商品ID（仅 order 时需要）
     * @param quantity 数量（仅 order 时需要）
     * @return 结果描述
     */
    @Tool(name = "mcp_milk_tea",
            description = "查询奶茶菜单或下单购买。action 必填：'menu' 查菜单，'order' 下单；下单时需提供 id 和 quantity。"
    )
    public String mcpMilkTea(String action, String id, String quantity) {
        log.info("调用 mcp_milk_tea 工具: action={}, id={}, quantity={}", action, id, quantity);

        if ("menu".equals(action)) {
            // 返回菜单字符串（或 JSON，但 MCP 通常期望 string）
            String menuStr = menu.stream()
                    .map(item -> item.get("id") + ": " + item.get("name") + " - ¥" + item.get("price"))
                    .collect(Collectors.joining("\n"));
            return "当前菜单:\n" + menuStr;
        }

        if ("order".equals(action)) {
            if (id == null || id.isEmpty() || quantity == null || quantity.isEmpty()) {
                return "下单失败：缺少商品ID或数量";
            }

            // 查找商品
            Map<String, Object> item = menu.stream()
                    .filter(m -> m.get("id").equals(id))
                    .findFirst()
                    .orElse(null);

            if (item == null) {
                return "无效的商品ID: " + id;
            }

            try {
                int qty = Integer.parseInt(quantity);
                double total = (double) item.get("price") * qty;
                String result = String.format("下单成功：%s x %d，总价：%.2f 元", item.get("name"), qty, total);
                log.info("订单完成: {}", result);
                return result;
            } catch (NumberFormatException e) {
                return "数量必须是数字";
            }
        }

        return "不支持的操作类型，请使用 'menu' 或 'order'";
    }

    //---------------
    public List<Map<String, Object>> menu() {
        log.info("调用：查看菜单");
        return menu;
    }

}
