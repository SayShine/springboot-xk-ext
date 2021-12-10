package com.xk.queue;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-12-09 14:33
 * 延迟队列服务
 */
public interface DelayQueueService extends InitializingBean, DisposableBean {

    /**
     * 延迟队列添加任务
     * @param runnable 任务
     * @param delayTime 延迟执行时间
     * @param timeUnit 时间单位
     * @return 加入任务队列是否成功
     */
    boolean add(@Nonnull Runnable runnable, long delayTime, @Nonnull TimeUnit timeUnit);
}
