package com.xk.queue;

import com.sun.istack.internal.NotNull;
import org.springframework.util.Assert;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-12-09 14:41
 */
public class DelayJob implements Delayed {

    /**
     * 任务
     */
    private final Runnable runnable;

    /**
     * 实际执行时间
     */
    private final long executeTime;

    public DelayJob(@NotNull Runnable runnable, long delayTime, @NotNull TimeUnit timeUnit) {
        Assert.notNull(runnable, "runnable can not be null!");
        Assert.notNull(timeUnit, "timeunit can not be null!");
        this.runnable = runnable;
        this.executeTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(delayTime, timeUnit);
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
        return unit.convert(executeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(@NotNull Delayed delayed) {
        DelayJob delayJob = (DelayJob) delayed;
        return (int) (this.executeTime - delayJob.executeTime);
    }

    public Runnable getRunnable(){
        return this.runnable;
    }
}
