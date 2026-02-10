package com.jhzhao.alibaba.mcp.demo.controller;

import com.jhzhao.alibaba.mcp.demo.entity.vo.OrderInfo;
import com.jhzhao.alibaba.mcp.demo.service.MilkTeaOrderService;
import com.jhzhao.alibaba.result.ResultBody;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Author zhaojh0912
 * Description 测试订单的接口
 * CreateDate 2026/2/08 16:02
 * Version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/milktea")
public class MilkTeaController {

    @Resource
    private MilkTeaOrderService milkTeaOrderService;

    //查询订菜单的接口
    @GetMapping("/menu")
    public ResultBody<List<Map<String, Object>>> getMenu() {
        log.info("controller 层 的调用：查看菜单");
        List<Map<String, Object>> menu = milkTeaOrderService.menu();
        return ResultBody.success(menu);
    }

    //下订单
    @PostMapping("/order")
    public ResultBody<String> placeOrder(@RequestBody @Valid OrderInfo order) {
        log.info("controller 层 的调用：下订单");
        return milkTeaOrderService.order(order);
    }
}
