package com.jhzhao.alibaba.config;

import com.jhzhao.alibaba.properties.RedisProperties;
import com.jhzhao.alibaba.utils.RedisUtil;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redis的序列化工具
 */
@Configuration
@ConditionalOnClass(RedisTemplate.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(RedisProperties.class)
@ConditionalOnProperty(prefix = "spring.data.redis.fastjson", name = "enable", havingValue = "true", matchIfMissing = true)
public class RedisTemplateConfig {

    /**
     * 自动注入 Spring Boot 已创建好的 RedisConnectionFactory
     * 它会根据配置自动识别：单机 / 哨兵 / 集群
     */
    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Key: String
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value: FastJSON2
        FastJson2JsonRedisSerializer<Object> fastJsonSerializer = new FastJson2JsonRedisSerializer<>(Object.class);
        template.setValueSerializer(fastJsonSerializer);
        template.setHashValueSerializer(fastJsonSerializer);

        // 默认序列化器也设置为 FastJSON2
        template.setDefaultSerializer(fastJsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisUtil redisUtil(RedisTemplate<String, Object> redisTemplate) {
        return new RedisUtil(redisTemplate);
    }
}
