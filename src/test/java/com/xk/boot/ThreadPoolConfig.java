package com.xk.boot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-12-09 16:27
 */
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ExecutorService executorService(){
        return new ThreadPoolExecutor(4, 10, 1, TimeUnit.HOURS, new LinkedBlockingQueue<>(1000));
    }

}
