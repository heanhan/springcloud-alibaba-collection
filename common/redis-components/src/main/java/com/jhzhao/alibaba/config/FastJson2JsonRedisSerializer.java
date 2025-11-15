package com.jhzhao.alibaba.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;


/**
 * Author zhaojh0912
 * Description 自定义序列化
 * CreateDate 2025/11/15 14:00
 * Version 1.0
 */
public class FastJson2JsonRedisSerializer<T> implements RedisSerializer<T> {

    private final Class<T> clazz;

    public FastJson2JsonRedisSerializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        try {
            return JSON.toJSONBytes(t, JSONWriter.Feature.WriteClassName);
        } catch (Exception e) {
            throw new SerializationException("Could not serialize", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return JSON.parseObject(bytes, clazz, JSONReader.Feature.SupportAutoType);
        } catch (Exception e) {
            throw new SerializationException("Could not deserialize", e);
        }
    }
}
