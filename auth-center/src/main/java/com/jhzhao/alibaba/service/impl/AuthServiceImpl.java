package com.jhzhao.alibaba.service.impl;

import com.jhzhao.alibaba.entity.SysUser;
import com.jhzhao.alibaba.repository.SysMenuRepository;
import com.jhzhao.alibaba.repository.SysRoleMenuRepository;
import com.jhzhao.alibaba.repository.SysRoleRepository;
import com.jhzhao.alibaba.repository.SysUserRepository;
import com.jhzhao.alibaba.repository.SysUserRoleRepository;
import com.jhzhao.alibaba.result.ResultBody;
import com.jhzhao.alibaba.security.JwtTokenProvider;
import com.jhzhao.alibaba.service.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2025/11/15 13:33
 * Version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final SysUserRepository userRepository;

    private final SysUserRoleRepository userRoleRepository;

    private final SysRoleRepository roleRepository;

    private final SysRoleMenuRepository roleMenuRepository;

    private final SysMenuRepository menuRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public ResultBody login(String username, String password) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        if (!user.getEnabled()) {
            throw new DisabledException("用户已被禁用");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("密码错误");
        }

        List<String> roles = userRoleRepository.findByUserId(user.getId()).stream()
                .map(ur -> roleRepository.findById(ur.getRoleId()))
                .filter(Optional::isPresent)
                .map(r -> r.get().getRoleCode())
                .toList();

        String token = jwtTokenProvider.generateToken(user, roles);

        return new ResultBody(token, user.getNickname(), roles);
    }
}
