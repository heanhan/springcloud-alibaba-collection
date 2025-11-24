package com.jhzhao.alibaba.service.impl;

import com.jhzhao.alibaba.entity.SysUser;
import com.jhzhao.alibaba.model.vo.UserRegisterVO;
import com.jhzhao.alibaba.repository.SysUserRepository;
import com.jhzhao.alibaba.service.SysUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2025/11/15 21:06
 * Version 1.0
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    @Resource
    private SysUserRepository sysUserRepository;
    /**
     * 用户注册
     *
     * @param request
     * @return
     */
    @Override
    public SysUser register(UserRegisterVO request) {
        return null;
    }
}
