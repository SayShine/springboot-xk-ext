package com.xk.queue.impl;

import com.xk.queue.BatchQueueService;
import com.xk.queue.base.BatchConsumeBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.List;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-12-28 10:00
 * 批量消费队列
 */
public abstract class AbstractBatchQueueService<T> implements BatchQueueService<T>, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(AbstractBatchQueueService.class);

    private final BatchConsumeBlockingQueue<T> queue;

    public AbstractBatchQueueService(int capacity, int batchConsumeSize, int maxWaitSize, long maxWaitTime){
        this.queue = new BatchConsumeBlockingQueue<>(capacity, batchConsumeSize, maxWaitSize, maxWaitTime, this::batchConsume);
    }

    @Override
    public void add(T t){
        try {
            this.queue.add(t);
        }catch (Exception e){
            log.error("添加元素失败,{}", t);
            throw e;
        }
    }

    @Override
    public void destroy() throws Exception {
        int destroyWaitTimes = 0;
        while (!this.queue.isEmpty()) {
            if (destroyWaitTimes++ > 3){
                return;
            }
            Thread.sleep(2000);
        }
    }

    private void batchConsume(List<T> list) {
        batchConsumeMsg(list);
    }
}
