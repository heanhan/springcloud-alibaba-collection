package com.jhzhao.alibaba.controller;


import com.jhzhao.alibaba.entity.Commodity;
import com.jhzhao.alibaba.model.vo.CommodityVo;
import com.jhzhao.alibaba.service.CommodityService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/commodity")
@RestController
public class CommodityController {

    @Resource
    private CommodityService commodityService;


    @PostMapping(value = "/findAllCommodity")
    private List<Commodity> findAllCommodity(@RequestBody CommodityVo vo){
        List<Commodity> allCommodityList = commodityService.findAllCommodityList(vo);
        return allCommodityList;
    }

    @PostMapping(value = "/addCommodity")
    private String addCommodity(@RequestBody Commodity param){
        commodityService.addCommodity(param);
        return "添加成功!";
    }


}
