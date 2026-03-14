package com.jhzhao.alibaba.service.impl;

import com.jhzhao.alibaba.dao.CommodityRepository;
import com.jhzhao.alibaba.entity.Commodity;
import com.jhzhao.alibaba.entity.CommodityOrder;
import com.jhzhao.alibaba.model.vo.CommodityBuyVo;
import com.jhzhao.alibaba.model.vo.CommodityVo;
import com.jhzhao.alibaba.service.CommodityOrderService;
import com.jhzhao.alibaba.service.CommodityService;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;



@Slf4j
@Service
public class CommodityServiceImpl implements CommodityService {

    @Resource
    private CommodityRepository commodityRepository;


    @Resource
    private CommodityOrderService commodityOrderService;


    /**
     * 查询所有列表
     * @param vo 条件
     * @return
     */
    @Override
    public List<Commodity> findAllCommodityList(CommodityVo vo) {
        List<Commodity> all = null;
        if(ObjectUtils.isEmpty(vo)){
            all = commodityRepository.findAll();
        }
        all = commodityRepository.findAll();
        return all;
    }

    /**
     * 添加商品
     *
     * @param param
     */
    @Override
    @Transactional
    public String addCommodity(CommodityBuyVo param) {
        log.info("通过 http请求 处理: 用户名：{}，奶茶名称:{}, 杯数：{}", param.getPhone(),param.getCommodityName(), param.getQuantity());
        List<Commodity> allCommodityList = this.findAllCommodityList(null);

        if (!StringUtils.hasText(param.getPhone()) || !StringUtils.hasText(param.getCommodityName()) || param.getQuantity() == null) {
            return "下单失败：缺少手机号/商品ID/数量";
        }
        // 查找商品
        Commodity commodity = allCommodityList.stream()
                .filter(m -> m.getComodityName().equals(param.getCommodityName()))
                .findFirst()
                .orElse(null);

        if (commodity == null) {
            return "无效的商品明湖城那个: " + param.getCommodityName();
        }
        try {
            double total = commodity.getPrice() * param.getQuantity();
            String result = String.format("下单成功：%s x %d，总价：%.2f 元", commodity.getComodityName(), param.getQuantity(), total);
            log.info("订单完成: {}", result);
            CommodityOrder order = new CommodityOrder();
            order.setPhone(param.getPhone());
            order.setComodityCode(commodity.getComodityCode());
            order.setComodityName(commodity.getComodityName());
            order.setPrice(commodity.getPrice());
            order.setCount(param.getQuantity());
            order.setTotalPrice(total);
            order.setCreateTime(new Date());
            order.setUpdateTime(new Date());
            commodityOrderService.saveCommodityOrder(order);
            return result;
        } catch (NumberFormatException e) {
            return "数量必须是数字";
        }
    }


}
