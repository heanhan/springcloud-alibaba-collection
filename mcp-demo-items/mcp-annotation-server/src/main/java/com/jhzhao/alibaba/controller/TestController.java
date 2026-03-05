package com.jhzhao.alibaba.controller;


import com.jhzhao.alibaba.entity.Commodity;
import com.jhzhao.alibaba.service.CommodityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/milktea")
public class TestController {

    @Autowired
    private CommodityService commodityService;

    @PostMapping("/menu")
    public ResponseEntity<Map<String, Object>> listMenu() {
        try {
            List<Commodity> list = commodityService.findAllCommodityList(null);
            log.info("在controller中查看奶茶的菜单");
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "查询成功");
            result.put("count", list.size());
            result.put("menu", list);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "查询菜单失败：" + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
