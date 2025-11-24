package com.jhzhao.alibaba.security;

import com.jhzhao.alibaba.utils.RedisUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 双令牌 Redis 缓存工具
 * access_token 有效期 2 小时
 * refresh_token 有效期 24 小时
 */
@Component
public class TokenCache {

    @Resource
    private RedisUtil redisUtil;

    /* ---------- key 前缀 ---------- */
    private static final String ACCESS_KEY  = "auth:access:";
    private static final String REFRESH_KEY = "auth:refresh:";

    /* ---------- 存 ---------- */
    public void saveAccess(String username, String token, long seconds) {
        redisUtil.set(ACCESS_KEY + username, token,seconds,TimeUnit.SECONDS);
    }

    public void saveRefresh(String username, String token, long seconds) {
        redisUtil.set(REFRESH_KEY + username, token,seconds,TimeUnit.SECONDS);
    }

    /* ---------- 取 ---------- */
    public String getAccess(String username) {
        return redisUtil.get(ACCESS_KEY + username).toString();
    }

    public String getRefresh(String username) {
        return redisUtil.get(REFRESH_KEY + username).toString();
    }

    /* ---------- 删 ---------- */
    public void deleteTokens(String username) {
        redisUtil.delete(ACCESS_KEY + username);
        redisUtil.delete(REFRESH_KEY + username);
    }

    /* ---------- 续期（可选） ---------- */
    public void expireAccess(String username, long seconds) {
        redisUtil.expire(ACCESS_KEY + username, seconds, TimeUnit.SECONDS);
    }

    public void expireRefresh(String username, long seconds) {
        redisUtil.expire(REFRESH_KEY + username, seconds, TimeUnit.SECONDS);
    }
}