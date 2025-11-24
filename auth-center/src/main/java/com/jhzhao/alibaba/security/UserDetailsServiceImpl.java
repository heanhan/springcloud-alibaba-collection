package com.jhzhao.alibaba.security;

import com.jhzhao.alibaba.entity.SysUser;
import com.jhzhao.alibaba.repository.SysUserRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private SysUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userRepository.findByUsername(username);
        if(ObjectUtils.isEmpty(user)){
            throw new UsernameNotFoundException("用户不存在");
        }
        //手动查询角色和权限
        Set<String> roleCodes = userRepository.findRoleCodesByUserId(user.getId());
        Set<String> permissions = userRepository.findPermissionsByUserId(user.getId());
        user.setRoleCodes(roleCodes);
        user.setPermissions(permissions);
        return user;
    }
}
