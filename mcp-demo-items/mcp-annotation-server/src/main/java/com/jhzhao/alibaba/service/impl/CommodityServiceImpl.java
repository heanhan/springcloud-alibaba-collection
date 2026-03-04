package com.jhzhao.alibaba.service.impl;

import com.jhzhao.alibaba.dao.CommodityRepository;
import com.jhzhao.alibaba.entity.Commodity;
import com.jhzhao.alibaba.model.vo.CommodityVo;
import com.jhzhao.alibaba.service.CommodityService;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
