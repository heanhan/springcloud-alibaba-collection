package com.jhzhao.alibaba.controller;


import com.jhzhao.alibaba.entity.Commodity;
import com.jhzhao.alibaba.model.vo.CommodityBuyVo;
import com.jhzhao.alibaba.model.vo.CommodityOrderBuyVo;
import com.jhzhao.alibaba.model.vo.CommodityVo;
import com.jhzhao.alibaba.service.CommodityOrderService;
import com.jhzhao.alibaba.service.CommodityService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/commodity")
@RestController
public class CommodityController {

    @Resource
    private CommodityService commodityService;

    @Resource
    private CommodityOrderService commodityOrderService;


    /**
     * 查新奶茶的菜单
     * @param vo
     * @return
     */
    @PostMapping(value = "/findAllCommodity")
    private List<Commodity> findAllCommodity(@RequestBody CommodityVo vo){
        log.info("通过http 端点访问 api 接口 findAllCommodity ");
        List<Commodity> allCommodityList = commodityService.findAllCommodityList(vo);
        return allCommodityList;
    }

    /**
     * 对奶茶进行下单
     * @param param
     * @return
     */
    @PostMapping(value = "/addCommodity")
    private String addCommodity(@RequestBody CommodityBuyVo param){
        log.info("通过http 端点访问 api 接口 addCommodity ");
        String result = commodityService.addCommodity(param);
        return result;
    }

    /**
     * 查询奶茶的订单  根据条件
     * @param param
     * @return
     */
    @PostMapping(value = "/findCommodityOrderInfo")
    private String findCommodityOrderInfo(@RequestBody CommodityOrderBuyVo param){
        log.info("通过http 端点访问 api 接口 findCommodityOrderInfo ");
        String result = commodityOrderService.findCommodityOrderInfo(param);
        return result;
    }


}
