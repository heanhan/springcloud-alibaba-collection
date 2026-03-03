package com.jhzhao.alibaba.service.impl;

import com.jhzhao.alibaba.dao.CommodityRepository;
import com.jhzhao.alibaba.entity.Commodity;
import com.jhzhao.alibaba.model.vo.CommodityVo;
import com.jhzhao.alibaba.service.CommodityService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;


@Service
public class CommodityServiceImpl implements CommodityService {

    @Resource
    private CommodityRepository commodityRepository;


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
    public void addCommodity(Commodity param) {
        commodityRepository.save(param);
    }
}
