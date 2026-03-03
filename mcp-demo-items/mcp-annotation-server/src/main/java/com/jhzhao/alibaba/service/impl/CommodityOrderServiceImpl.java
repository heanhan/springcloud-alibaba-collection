package com.jhzhao.alibaba.service.impl;

import com.jhzhao.alibaba.dao.CommodityOrderRepository;
import com.jhzhao.alibaba.entity.CommodityOrder;
import com.jhzhao.alibaba.service.CommodityOrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CommodityOrderServiceImpl  implements CommodityOrderService {


    @Resource
    private CommodityOrderRepository commodityOrderRepository;
    /**
     * 保存订单信息
     *
     * @param commodityOrder
     * @return
     */
    @Override
    @Transactional
    public CommodityOrder saveCommodityOrder(CommodityOrder commodityOrder) {
        return commodityOrderRepository.save(commodityOrder);
    }
}
