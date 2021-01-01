package pers.interview.springboot.service;

import org.junit.jupiter.api.Test;
import pers.interview.springboot.service.impl.ConcurrentRateLimitService;
import pers.interview.springboot.service.impl.RateLimitServiceImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class RateLimitServiceTest {

    public static final int THRESHOLD = 100;
    private ExecutorService executorService=Executors.newFixedThreadPool(20);

    /**
     * 单机限流测试
     */
    @Test
    void tryAcquire() {
        RateLimitServiceImpl rateLimitService=new RateLimitServiceImpl();
        for (int i = 0; i < 10; i++) {
            boolean acquire = rateLimitService.tryAcquire(System.currentTimeMillis());
            System.out.println(acquire);
        }
    }

    /**
     * 并发测试，存在并发问题
     */
    @Test
    void concurrentTryAcquire() throws InterruptedException {
        RateLimitService rateLimitService=new RateLimitServiceImpl();
        rateLimit(rateLimitService);
    }

    @Test
    void concurrentTryAcquire1() throws InterruptedException {
        RateLimitService concurrentRateLimitService=new ConcurrentRateLimitService();
        rateLimit(concurrentRateLimitService);
    }

    private void rateLimit(RateLimitService rateLimitService) throws InterruptedException {
        AtomicInteger counter=new AtomicInteger();
        for (int i = 0; i < THRESHOLD; i++) {
            executorService.submit(()->{
                boolean acquire;
                do {
                    acquire = rateLimitService.tryAcquire(System.currentTimeMillis());
                    if (acquire){
                        counter.incrementAndGet();
                    }
                }while (!acquire);
            });
        }
        executorService.awaitTermination(2, TimeUnit.SECONDS);
        System.out.println(counter.get());
    }




}