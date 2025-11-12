package com.jhzhao.alibaba.annotation;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class PreventRepeatAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(preventRepeat)")
    public Object around(ProceedingJoinPoint pjp, PreventRepeat preventRepeat) throws Throwable {
        String key = "prevent:repeat:" + (preventRepeat.value().isEmpty() ? pjp.getSignature().toLongString() : preventRepeat.value());
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", preventRepeat.expireSeconds(), TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(success)) {
            try {
                return pjp.proceed();
            } finally {
                redisTemplate.delete(key);
            }
        } else {
            throw new RuntimeException("请勿重复提交");
        }
    }
}