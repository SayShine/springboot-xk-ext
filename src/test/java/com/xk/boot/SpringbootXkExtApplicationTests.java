package com.xk.boot;

import com.xk.queue.DelayQueueService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class SpringbootXkExtApplicationTests {

    @Resource
    private DelayQueueService delayQueueService;

    @Test
    void testDelayQueueService() throws InterruptedException {
        System.out.println(System.currentTimeMillis());
        delayQueueService.add(()-> System.out.println(System.currentTimeMillis()), 3, TimeUnit.SECONDS);
        TimeUnit.SECONDS.sleep(100);
    }

}
