package pers.interview.ribbon;

import pers.interview.ribbon.common.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮训调度规则
 *
 * @description:
 * @author: haochencheng
 * @create: 2019-09-11 18:08
 **/
public class RoundRobinRule {

    private List<Server> allServerList;

    private AtomicInteger nextServerCyclicCounter;

    private static AtomicInteger counter=new AtomicInteger();

    public RoundRobinRule(){
        nextServerCyclicCounter=new AtomicInteger(0);
        allServerList = Collections.synchronizedList(new ArrayList<>());
    }

    public static void main(String[] args) throws InterruptedException {
        RoundRobinRule roundRobinRule=new RoundRobinRule();
        roundRobinRule.addServer(new Server("1", 80));
        roundRobinRule.addServer(new Server("2", 80));
        roundRobinRule.addServer(new Server("3", 80));
//        singleThread(roundRobinRule);
        multiThread(roundRobinRule);
    }

    private static void singleThread(RoundRobinRule roundRobinRule) {
        for (int i = 0; i < 100; i++) {
            chooseServer(roundRobinRule);
        }
    }

    private static void multiThread(RoundRobinRule roundRobinRule) throws InterruptedException {
        CountDownLatch countDownLatch=new CountDownLatch(10);
        Runnable runnable = () -> {
            countDownLatch.countDown();
            chooseServer(roundRobinRule);
        };
        for (int i = 0; i < 100; i++) {
            new Thread(runnable).run();
        }
        countDownLatch.await();
    }

    private static void chooseServer(RoundRobinRule roundRobinRule) {
        Server choose = roundRobinRule.choose();
        System.out.println(counter.incrementAndGet()+" - "+choose.toString());
    }

    public Server choose() {
        Server server = null;
        int count = 0;
        int serverCount = allServerList.size();
        while (server == null && count++ < 10) {
            int nextServerIndex = incrementAndGetModulo(serverCount);
            server = allServerList.get(nextServerIndex);
            if (server == null) {
                /* Transient. */
                Thread.yield();
                continue;
            }
        }
        if (count >= 10) {
            System.out.println("No available alive servers after 10 tries");
        }
        return server;
    }

    private int incrementAndGetModulo(int modulo){
        for (;;){
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next)){
                return next;
            }
            System.out.printf("cas失败 : current-%d,next-%d"+current,next);
        }
    }

    private void addServer(Server server){
        allServerList.add(server);
    }


}
