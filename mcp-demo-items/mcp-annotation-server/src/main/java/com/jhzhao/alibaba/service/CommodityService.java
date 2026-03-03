package com.jhzhao.alibaba.service;

import com.jhzhao.alibaba.entity.Commodity;
import com.jhzhao.alibaba.model.vo.CommodityVo;

import java.util.List;

public interface CommodityService {

    /**
     * 查询所有列表
     * @param vo 条件
     * @return
     */
    List<Commodity> findAllCommodityList(CommodityVo vo);

    /**
     * 添加商品
     * @param param
     */
    void addCommodity(Commodity param);
}
