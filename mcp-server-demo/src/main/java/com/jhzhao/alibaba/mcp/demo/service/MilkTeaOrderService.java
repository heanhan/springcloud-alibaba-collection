package com.jhzhao.alibaba.mcp.demo.service;

import com.jhzhao.alibaba.mcp.demo.entity.vo.OrderInfo;
import com.jhzhao.alibaba.result.ResultBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2026/2/10 18:06
 * Version 1.0
 */
@Slf4j
@Service
public class MilkTeaOrderService {

    //这个是订单 列表 测试数据
    private final List<Map<String, Object>> menu = Arrays.asList(
            Map.of("id", 1, "name", "原味奶茶", "price", 5.0),
            Map.of("id", 2, "name", "绿奶茶", "price", 5.5),
            Map.of("id", 3, "name", "珍珠奶茶", "price", 6.0)
    );

    @Tool(description = "获取当前奶茶店的菜单")
    public List<Map<String, Object>> menu() {
        log.info("调用：查看菜单");
        return menu;
    }

    @Tool(description = "下订单")
    public ResultBody order(OrderInfo order){
        log.info("开始下单：饮品ID:{}，数量：{}",order.getId(),order.getQuantity());
        //解析订单参数
        Integer id = Integer.valueOf(order.getId());
        Integer quantity = Integer.valueOf(order.getQuantity());
        //遍历查询订单
        Map<String, Object> item = menu.stream()
                .filter(m -> m.get("id").equals(id))
                .findFirst()
                .orElse(null);

        if (item == null) {
            return ResultBody.error("无效的 item ID");
        }
        //计算订单信息
        double total = (double) item.get("price") * quantity;
        log.info("订单详情，饮品名称：{} ,下单数量：{},总价格RMB：{} 元" ,item.get("name"),quantity,total);
        return ResultBody.success("下单数量成功 数量: " + quantity + " x " + item.get("name") + " ,总价格：" + total);
    }
}
