package com.xk.boot.lock;

import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-03-29 17:39
 * redis实现的原子操作，如分布式锁，自增原子类
 */
public class RedisAtomicClient {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * lua脚本：value自增同时设置失效时间
     */
    private static final String INCR_BY_WITH_TIMEOUT = "local v;" +
            "v = redis.call('incrBy',KEYS[1],ARGV[1]);" +
            "if tonumber(v) == tonumber(ARGV[1]) " +
            "then\n" +
            "    redis.call('expire',KEYS[1],ARGV[2])\n" +
            "end\n" +
            "return v";

    /**
     * lua脚本：比较redis中的值与预期值，相同则删除
     */
    private static final String COMPARE_AND_DELETE =
            "if redis.call('get',KEYS[1]) == ARGV[1]\n" +
                    "then\n" +
                    "    return redis.call('del',KEYS[1])\n" +
                    "else\n" +
                    "    return 0\n" +
                    "end";


    public RedisAtomicClient(RedisTemplate<String, ?> redisTemplate) {
        Assert.notNull(redisTemplate,"redisTemplate cannot be null, please check this config!");
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisTemplate.getConnectionFactory());
        stringRedisTemplate.afterPropertiesSet();
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * redis原子类自增
     *
     * @param key 键
     * @param delta 自增数，可为负值
     * @param exp 过期时间
     * @param timeUnit 时间单位
     * @return 自增后的数
     */
    public Long incrBy(String key, long delta, long exp, TimeUnit timeUnit) {
        List<String> keys = new ArrayList<>();
        keys.add(key);
        long timeoutSeconds = TimeUnit.SECONDS.convert(exp, timeUnit);
        String[] args = new String[2];
        args[0] = String.valueOf(delta);
        args[1] = String.valueOf(timeoutSeconds);
        Long currentVal = stringRedisTemplate.execute(new DefaultRedisScript<>(INCR_BY_WITH_TIMEOUT, Long.class), keys, args);

        if (currentVal == null) {
            return null;
        }
        return currentVal;
    }

    /**
     * 获取redis锁的方法，获取不到则返回空
     *
     * @param key 锁的key值
     * @param exp 锁的失效时间（单位：秒）
     * @return redis锁
     */
    public RedisLock getLock(String key, long exp) {
        return getLock(key, exp, 0, 0);
    }

    /**
     * 重试会造成大量的线程挂起，请尽量避免使用此方法
     * 获取redis锁的方法，获取不到则返回空，可以设置最大重试次数与最大重试时间
     *
     * @param key                     缓存的key值
     * @param exp                     超时时间
     * @param maxRetryTime            最大重试次数
     * @param retryIntervalTimeMillis 重试前的等待时间
     * @return redis锁
     */
    public RedisLock getLock(final String key, final long exp, long maxRetryTime, long retryIntervalTimeMillis) {
        maxRetryTime += 1;
        final String value = UUID.randomUUID().toString();

        for (int i = 0; i < maxRetryTime; i++) {
            Boolean result = stringRedisTemplate.execute((RedisCallback<Boolean>) connection ->
                    connection.set(key.getBytes(), value.getBytes(), Expiration.seconds(exp), RedisStringCommands.SetOption.ifAbsent()));
            if (result != null && result) {
                return new RedisLockInner(stringRedisTemplate, key, value);
            }

            if (retryIntervalTimeMillis > 0) {
                try {
                    Thread.sleep(retryIntervalTimeMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
        }
        return null;
    }

    private static class RedisLockInner implements RedisLock{

        private final StringRedisTemplate stringRedisTemplate;

        private final String key;

        private final String expectedValue;

        private final long threadId;

        protected RedisLockInner(StringRedisTemplate redisTemplate, String key, String expectedValue) {
            this.stringRedisTemplate = redisTemplate;
            this.key = key;
            this.expectedValue = expectedValue;
            this.threadId = Thread.currentThread().getId();
        }

        /**
         * 自动释放redis分布式锁。当前线程id与添加分布式锁的线程id不同，无法释放锁
         * redis中的value与期待value不一致时，无法释放锁
         */
        @Override
        public void close() throws IllegalAccessException {
            if (Thread.currentThread().getId() != threadId) {
                throw new IllegalAccessException("thread id error!，can not release lock!");
            }
            List<String> keys = new ArrayList<>();
            keys.add(key);
            stringRedisTemplate.execute(new DefaultRedisScript<>(COMPARE_AND_DELETE, Long.class), keys, expectedValue);
        }

    }

}
