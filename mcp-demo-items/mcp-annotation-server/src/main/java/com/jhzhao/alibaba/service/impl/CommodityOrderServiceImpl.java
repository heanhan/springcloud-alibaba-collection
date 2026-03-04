package com.jhzhao.alibaba.service.impl;

import com.jhzhao.alibaba.dao.CommodityOrderRepository;
import com.jhzhao.alibaba.entity.Commodity;
import com.jhzhao.alibaba.entity.CommodityOrder;
import com.jhzhao.alibaba.service.CommodityOrderService;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


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

    /**
     * 动条件查询 用户的奶茶订单
     *
     * @param phone        手机号
     * @param comodityName 奶茶名称
     * @return
     */
    @Override
    public List<CommodityOrder> findAllCommodityOrder(String phone, String comodityName) {
        Specification<CommodityOrder> spec = new Specification<CommodityOrder>() {
            @Override
            public Predicate toPredicate(Root<CommodityOrder> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                /**
                 * root 拿到当前对象的所有信息
                 * Path 包装 从实体中获取的属性
                 * Predicate 为属赋值的对象
                 */
                //1、创建一个Prddicate 添加封装集合
                List<Predicate> predicateList=new ArrayList<>();
                //手机号
                if(phone!=null&&!"".equals(phone)){
                    Path<CommodityOrder> c1 = root.get("phone");
                    predicateList.add(criteriaBuilder.equal(c1.as(String.class), phone));
                }
                //商品名
                if(StringUtils.hasText(comodityName)){
                    Path<CommodityOrder> c2 = root.get("comodityName");
                    predicateList.add(criteriaBuilder.equal(c2.as(String.class),comodityName));
                }
                return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
            }
        };
        List<CommodityOrder> all = commodityOrderRepository.findAll(spec);
        return all;
    }
}
