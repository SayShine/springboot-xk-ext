package com.xk.pipeline.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-11-05 15:51
 */
@Component
public class RedisListOperationServiceImpl implements RedisListOperationService{

    private static final Logger log = LoggerFactory.getLogger(RedisListOperationServiceImpl.class);

    @Override
    public <T> Boolean pushAndExpire(RedisTemplate<String, T> redisTemplate, String key, List<T> valueList, long timeout, TimeUnit timeUnit) {
        if (CollectionUtils.isEmpty(valueList)) {
            return false;
        }

        long timeoutSeconds = TimeUnit.SECONDS.convert(timeout, timeUnit);
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            connection.lPush(key.getBytes(), rawValues(valueList, redisTemplate.getValueSerializer()));
            connection.expire(key.getBytes(), timeoutSeconds);
            return true;
        });
    }

    @Override
    public <T> List<T> lPopupAll(RedisTemplate<String, T> redisTemplate, String key) {
        List<byte[]> list = redisTemplate.execute((RedisCallback<List<byte[]>>) connection -> {
            List<byte[]> result = connection.lRange(key.getBytes(), 0, -1);
            long realSize = Optional.ofNullable(connection.lLen(key.getBytes())).orElse(0L);
            for (int i = 0; i < realSize; i++) {
                connection.lPop(key.getBytes());
            }
            return result;
        });
        if(CollectionUtils.isEmpty(list)){
            log.info("RedisListOperationServiceImpl lPopupWithSize, list is empty");
            return Collections.emptyList();
        }
        return list.stream().filter(Objects::nonNull).map(bytes -> (T)redisTemplate.getValueSerializer().deserialize(bytes)).collect(Collectors.toList());
    }

    /**
     * 集合转二维字节数组
     */
    @SuppressWarnings(value = "unchecked, rawtypes")
    private <T> byte[][] rawValues(Collection<T> values, RedisSerializer redisSerializer) {
        byte[][] rawValues = new byte[values.size()][];
        int i = 0;
        for (T value : values) {
            rawValues[i++] = redisSerializer.serialize(value);
        }
        return rawValues;
    }
}
