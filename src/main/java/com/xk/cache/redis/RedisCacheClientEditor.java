package com.xk.cache.redis;

import org.springframework.data.redis.core.RedisTemplate;

import java.beans.PropertyEditorSupport;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-10-20 10:23
 * 通过redisTemplate生成RedisCacheClient
 */
public class RedisCacheClientEditor extends PropertyEditorSupport {

    @Override
    public void setValue(Object value) {
        if (value instanceof RedisTemplate) {
            RedisCacheClient redisCacheClient = new RedisCacheClient((RedisTemplate)value);
            super.setValue(redisCacheClient);
        } else {
            throw new IllegalArgumentException("Editor supports only conversion of type " + RedisTemplate.class);
        }
    }
}
