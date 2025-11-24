package com.jhzhao.alibaba.service;

import com.jhzhao.alibaba.entity.SysUser;
import com.jhzhao.alibaba.model.vo.UserRegisterVO;
import jakarta.validation.Valid;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2025/11/15 21:02
 * Version 1.0
 */
public interface SysUserService {
    /**
     * 用户注册
     * @param request
     * @return
     */
    SysUser register(@Valid UserRegisterVO request);
}
