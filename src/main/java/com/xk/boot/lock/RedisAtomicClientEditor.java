package com.xk.boot.lock;

import com.xk.cache.redis.RedisCacheClient;
import org.springframework.data.redis.core.RedisTemplate;

import java.beans.PropertyEditorSupport;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-10-20 13:53
 * 通过redisTemplate生成RedisAtomicClient
 */
public class RedisAtomicClientEditor extends PropertyEditorSupport {

    @Override
    public void setValue(Object value) {
        if (value instanceof RedisTemplate) {
            RedisAtomicClient redisAtomicClient = new RedisAtomicClient((RedisTemplate)value);
            super.setValue(redisAtomicClient);
        } else {
            throw new IllegalArgumentException("Editor supports only conversion of type " + RedisTemplate.class);
        }
    }
}
