package com.xk.boot.lock;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-03-29 19:21
 * redis实现分布式锁，继承AutoCloseable是为了实现分布式锁的自动释放，详情请查看
 * {@link AutoCloseable#close()}
 */
public interface RedisLock extends AutoCloseable{

}
