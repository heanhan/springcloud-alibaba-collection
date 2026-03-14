package com.jhzhao.alibaba.service;

import com.jhzhao.alibaba.entity.CommodityOrder;
import com.jhzhao.alibaba.model.vo.CommodityOrderBuyVo;

import java.util.List;

public interface CommodityOrderService {


    /**
     * 保存订单信息
     * @return
     */
    CommodityOrder saveCommodityOrder(CommodityOrder commodityOrder);

    /**
     * 动条件查询 用户的奶茶订单
     * @param phone  手机号
     * @param comodityName 奶茶名称
     * @return
     */
    List<CommodityOrder> findAllCommodityOrder(String phone, String comodityName);

    String findCommodityOrderInfo(CommodityOrderBuyVo param);
}
