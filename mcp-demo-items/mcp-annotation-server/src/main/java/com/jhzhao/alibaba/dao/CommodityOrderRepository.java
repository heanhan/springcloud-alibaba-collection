package com.jhzhao.alibaba.dao;

import com.jhzhao.alibaba.entity.CommodityOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface CommodityOrderRepository extends JpaRepository<CommodityOrder,Integer>, JpaSpecificationExecutor<CommodityOrder> {
}
