package com.xk.queue;

import java.util.List;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-12-31 10:48
 */
public interface BatchQueueService<T> {

    /**
     * 添加元素
     * @param t 元素
     */
    void add(T t);

    /**
     * 批量消费元素
     * @param list 元素
     */
    void batchConsumeMsg(List<T> list);
}
