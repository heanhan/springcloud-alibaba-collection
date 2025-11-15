package com.jhzhao.alibaba;

import com.jhzhao.alibaba.annotation.PreventRepeatAspect;
import com.jhzhao.alibaba.aspect.RateLimitAspect;
import com.jhzhao.alibaba.config.RedisTemplateConfig;
import com.jhzhao.alibaba.lock.DistributedLock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        RedisTemplateConfig.class,
        RateLimitAspect.class,
        PreventRepeatAspect.class,
        DistributedLock.class
})
@EnableConfigurationProperties
public class RedisComponentsAutoConfiguration {
}
