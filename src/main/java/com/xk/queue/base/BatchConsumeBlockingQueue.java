package com.xk.queue.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-12-24 10:01
 * 批量消费阻塞队列
 */
public class BatchConsumeBlockingQueue<T> {

    private static final Logger logger = LoggerFactory.getLogger(BatchConsumeBlockingQueue.class);

    /**
     * 批量消费数量
     */
    private final int batchConsumeSize;

    /**
     * 最多累积数量，超过此阈值则会触发消费
     * 小于0，则不开启按量消费
     */
    private final int maxWaitSize;

    /**
     * 最大等待时间,超过此阈值则会触发消费
     * 小于0，则不开启延时消费
     */
    private final long maxWaitTime;

    /**
     * 消费者
     */
    private final Consumer<List<T>> consumer;

    /**
     * 消费队列
     */
    private final LinkedBlockingQueue<T> consumeQueue;

    /**
     * 消费锁
     */
    private final ReentrantLock consumeLock = new ReentrantLock();

    /**
     * 消费锁condition
     */
    private final Condition consumeCondition = consumeLock.newCondition();

    /**
     * 上一次消费时间
     */
    private volatile long lastConsumeTime = System.currentTimeMillis();

    public BatchConsumeBlockingQueue(int capacity, int batchConsumeSize, int maxWaitSize, long maxWaitTime, Consumer<List<T>> consumer) {
        this.batchConsumeSize = batchConsumeSize;
        this.maxWaitSize = maxWaitSize;
        this.maxWaitTime = maxWaitTime;
        this.consumer = consumer;
        if (capacity <= 0) {
            // 小于0视作无界队列
            this.consumeQueue = new LinkedBlockingQueue<>();
        } else {
            this.consumeQueue = new LinkedBlockingQueue<>(capacity);
        }
        // 初始化消费线程
        begin();
    }

    private void begin() {
        if (maxWaitTime <= 0 && maxWaitSize <= 0) {
            throw new IllegalArgumentException("batchConsume must choose time or size consume!");
        }
        new Thread(this::batchConsume).start();
        if (maxWaitTime > 0) {
            new Thread(this::delayConsume).start();
        }
    }

    public void add(T t) {
        boolean add = this.consumeQueue.add(t);
        if (add) {
            signalConsume();
        }
    }

    public boolean isEmpty() {
        return this.consumeQueue.isEmpty();
    }

    private void delayConsume() {
        while (true) {
            try {
                Thread.sleep(this.maxWaitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            signalConsume();
        }
    }

    private void batchConsume() {
        for (; ; ) {
            try {
                doBatchConsume();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error("消费失败", e);
            }

            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            if (consumeQueue.isEmpty()) {
                break;
            }
        }
    }

    private void doBatchConsume() throws InterruptedException {
        final LinkedBlockingQueue<T> consumeQueue = this.consumeQueue;
        final ReentrantLock consumeLock = this.consumeLock;
        final Condition consumeCondition = this.consumeCondition;
        List<T> t = new ArrayList<>();
        consumeLock.lockInterruptibly();
        try {
            while (needWait()) {
                consumeCondition.await();
            }
            consumeQueue.drainTo(t, this.batchConsumeSize);
            setLastConsumeTime();
        } finally {
            consumeLock.unlock();
        }
        consumer.accept(t);
    }

    private boolean needWait() {
        return sizeWait() && timeWait();
    }

    private boolean timeWait() {
        if (maxWaitTime <= 0) {
            return true;
        }
        return System.currentTimeMillis() - lastConsumeTime < maxWaitTime;
    }

    private boolean sizeWait() {
        if (this.maxWaitSize <= 0) {
            return true;
        }
        return this.consumeQueue.size() < this.maxWaitSize;
    }

    private void setLastConsumeTime() {
        this.lastConsumeTime = System.currentTimeMillis();
    }

    private void signalConsume() {
        final Lock lock = this.consumeLock;
        lock.lock();
        try {
            consumeCondition.signal();
        } finally {
            lock.unlock();
        }
    }
}
