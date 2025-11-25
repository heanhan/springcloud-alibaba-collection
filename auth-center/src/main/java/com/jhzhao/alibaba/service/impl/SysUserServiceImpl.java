package com.jhzhao.alibaba.service.impl;

import com.jhzhao.alibaba.entity.SysUser;
import com.jhzhao.alibaba.model.vo.UserRegisterVO;
import com.jhzhao.alibaba.repository.SysUserRepository;
import com.jhzhao.alibaba.service.SysUserService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    @Resource
    private PasswordEncoder passwordEncoder;
    /**
     * 用户注册
     *
     * @param request
     * @return
     */
    @Override
    public SysUser register(UserRegisterVO request) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(request,sysUser);
        sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));//对密码加密
        sysUser.setCreateTime(LocalDateTime.now());//创建时间
        sysUser.setUpdateTime(LocalDateTime.now());//更新时间
        SysUser save = sysUserRepository.save(sysUser);
        return save;
    }
}
