package com.jhzhao.alibaba.service;

import com.jhzhao.alibaba.entity.CommodityOrder;

public interface CommodityOrderService {


    /**
     * 保存订单信息
     * @return
     */
    CommodityOrder saveCommodityOrder(CommodityOrder commodityOrder);
}
