package com.jhzhao.alibaba.dao;

import com.jhzhao.alibaba.entity.Commodity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CommodityRepository extends JpaRepository<Commodity,Integer>, JpaSpecificationExecutor<Commodity> {

}
