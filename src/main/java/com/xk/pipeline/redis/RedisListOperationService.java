package com.xk.pipeline.redis;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-11-05 15:45
 */
public interface RedisListOperationService {

    /**
     * list入队并设置失效时间
     *
     * @param redisTemplate redis操作模板
     * @param key           键
     * @param valueList     入队的list
     * @param timeout       失效时间
     * @param timeUnit      时间单位
     * @param <T>           队列元素
     * @return 操作结果
     */
    <T> Boolean pushAndExpire(RedisTemplate<String, T> redisTemplate, String key, List<T> valueList, long timeout, TimeUnit timeUnit);

    /**
     * list元素全部出队
     *
     * @param redisTemplate redis操作模板
     * @param key           键值
     * @param <T>           队列元素
     * @return 出队的指定元素
     */
    <T> List<T> lPopupAll(RedisTemplate<String, T> redisTemplate, String key);
}
