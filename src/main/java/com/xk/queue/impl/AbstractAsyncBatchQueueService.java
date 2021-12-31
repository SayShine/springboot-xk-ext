package com.xk.queue.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-12-31 11:07
 * 异步消费-批量消费队列
 */
public class AbstractAsyncBatchQueueService<T> extends AbstractBatchQueueService<T> {

    @Resource
    private ExecutorService executorService;

    private static final Logger log = LoggerFactory.getLogger(AbstractAsyncBatchQueueService.class);

    public AbstractAsyncBatchQueueService(int capacity, int batchConsumeSize, int maxWaitSize, long maxWaitTime) {
        super(capacity, batchConsumeSize, maxWaitSize, maxWaitTime);
    }

    public void batchConsume(List<T> list) {
        executorService.execute(() -> {
            try {
                batchConsumeMsg(list);
            } catch (Exception e) {
                log.error("消费失败", e);
            }
        });
    }

    @Override
    public void batchConsumeMsg(List<T> list) {

    }
}
