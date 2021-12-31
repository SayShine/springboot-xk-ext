package com.xk.queue.impl;

import com.xk.queue.DelayQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-12-09 14:39
 * 延迟队列任务服务
 */
@Service
public class DelayQueueServiceImpl implements DelayQueueService {

    private static final Logger log = LoggerFactory.getLogger(DelayQueueServiceImpl.class);

    /**
     * 延迟任务队列容积，超过则会告警
     */
    private static final long MAX_CAPACITY = 100000;

    /**
     * 容器销毁时，队列继续消费的最长等待时间
     */
    private static final long MAX_DESTROY_WAIT_TIME = 10 * 60L;

    @Resource
    private ExecutorService delayExecutorService;

    private final DelayQueue<DelayJob> delayQueue = new DelayQueue<>();

    @Override
    public boolean add(@Nonnull Runnable runnable, long delayTime, @Nonnull TimeUnit timeUnit) {
        boolean result = delayQueue.add(new DelayJob(runnable, delayTime, timeUnit));
        if(delayQueue.size() > MAX_CAPACITY){
            log.warn("DelayQueueService size is too large! please check! current size={}, max size={}", delayQueue.size(), MAX_CAPACITY);
        }
        return result;
    }

    /**
     * 优雅关闭，容器销毁时，任务队列继续消费。
     * 第一次线程休息10s，接着20s，30s，直至消费完毕或者休息时间超过阈值
     */
    @Override
    public void destroy() throws Exception {
        int t = 1;
        int totalSleepTime = 0;
        while(!delayQueue.isEmpty()){
            int sleepTime = 10 * t++;
            TimeUnit.SECONDS.sleep(sleepTime);
            totalSleepTime += sleepTime;
            if(totalSleepTime > MAX_DESTROY_WAIT_TIME){
                log.warn("delayQueue wait too long, shut down! left task size={}", delayQueue.size());
                break;
            }
        }
    }

    /**
     * 容器启动时，任务队列就开始消费。
     * 只有在队列元素的时间到期后，元素才可以被取出
     * @see java.util.concurrent.DelayQueue#take()
     * @see DelayJob#getDelay(TimeUnit)
     */
    @Override
    public void afterPropertiesSet() {
        delayExecutorService.execute(() -> {
            do{
                try {
                    DelayJob delayJob = delayQueue.take();
                    delayExecutorService.execute(delayJob.getRunnable());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while (!Thread.currentThread().isInterrupted());
        });
    }

    static class DelayJob implements Delayed {

        /**
         * 任务
         */
        private final Runnable runnable;

        /**
         * 实际执行时间
         */
        private final long executeTime;

        public DelayJob(@Nonnull Runnable runnable, long delayTime, @Nonnull TimeUnit timeUnit) {
            Assert.notNull(runnable, "runnable can not be null!");
            Assert.notNull(timeUnit, "timeunit can not be null!");
            this.runnable = runnable;
            this.executeTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(delayTime, timeUnit);
        }

        @Override
        public long getDelay(@Nonnull TimeUnit unit) {
            return unit.convert(executeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(@Nonnull Delayed delayed) {
            DelayJob delayJob = (DelayJob) delayed;
            return (int) (this.executeTime - delayJob.executeTime);
        }

        public Runnable getRunnable(){
            return this.runnable;
        }
    }
}
