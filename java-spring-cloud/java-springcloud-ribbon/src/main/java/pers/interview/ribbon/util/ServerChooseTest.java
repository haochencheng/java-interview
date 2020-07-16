package pers.interview.ribbon.util;

import pers.interview.ribbon.common.IRule;
import pers.interview.ribbon.common.Server;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-09-11 18:57
 **/
public class ServerChooseTest {

    private static AtomicInteger counter = new AtomicInteger();

    public static void singleThread(IRule roundRobinRule) {
        for (int i = 0; i < 100; i++) {
            chooseServer(roundRobinRule);
        }
    }

    public static void multiThread(IRule iRule) throws InterruptedException {
        CountDownLatch countDownLatch=new CountDownLatch(10);
        Runnable runnable = () -> {
            countDownLatch.countDown();
            chooseServer(iRule);
        };
        for (int i = 0; i < 100; i++) {
            new Thread(runnable).run();
        }
        countDownLatch.await();
    }

    private static void chooseServer(IRule iRule) {
        Server choose = iRule.choose();
        System.out.println(counter.incrementAndGet()+" - "+choose.toString());
    }

}
