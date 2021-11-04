package com.xk.cache.support;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-03-26 10:13
 * 缓存客户端实际获取数据的入口，由调用方提供具体实现
 */
@FunctionalInterface
public interface CacheLoader<T> {

    /**
     * 实际获取数据的方法
     * @return 要加入缓存的数据
     */
     T load();
}
