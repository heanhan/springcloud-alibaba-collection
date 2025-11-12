package com.jhzhao.alibaba.aspect;

import com.jhzhao.alibaba.annotation.RateLimit;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LUA_SCRIPT = """
        local key = KEYS[1]
        local permits = tonumber(ARGV[1])
        local now = tonumber(redis.call('GET', key .. ':time') or 0)
        local current = tonumber(redis.call('GET', key) or 0)
        local elapsed = tonumber(redis.call('TIME')[1]) - now
        if elapsed > 1000 then
            redis.call('SET', key, permits)
            redis.call('SET', key .. ':time', redis.call('TIME')[1])
            return 1
        else
            if current + permits > 1000 then
                return 0
            else
                redis.call('INCRBY', key, permits)
                return 1
            end
        end
        """;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        String key = "rate:limit:" + rateLimit.key();
        RedisScript<Long> script = RedisScript.of(LUA_SCRIPT, Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(key), rateLimit.permitsPerSecond());
        if (result != null && result == 1) {
            return pjp.proceed();
        }
        throw new RuntimeException("请求过于频繁，请稍后再试");
    }
}
