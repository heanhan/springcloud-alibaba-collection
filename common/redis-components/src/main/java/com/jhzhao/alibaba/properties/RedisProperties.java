package com.jhzhao.alibaba.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Author zhaojh0912
 * Description 配置文件redis
 * CreateDate 2025/11/15 14:02
 * Version 1.0
 */
@Data
@ConfigurationProperties(prefix = "spring.data.redis.fastjson")
public class RedisProperties {
    private boolean enable = true;
}
