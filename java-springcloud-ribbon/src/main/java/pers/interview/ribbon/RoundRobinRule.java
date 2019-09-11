package pers.interview.ribbon;

import pers.interview.ribbon.common.IRule;
import pers.interview.ribbon.common.Server;
import pers.interview.ribbon.util.ServerChooseTest;

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
public class RoundRobinRule implements IRule {

    private List<Server> allServerList;

    private AtomicInteger nextServerCyclicCounter;

    public RoundRobinRule(){
        nextServerCyclicCounter=new AtomicInteger(0);
        allServerList = Collections.synchronizedList(new ArrayList<>());
    }

    public static void main(String[] args) throws InterruptedException {
        RoundRobinRule roundRobinRule=new RoundRobinRule();
        roundRobinRule.addServer(new Server("1", 80));
        roundRobinRule.addServer(new Server("2", 80));
        roundRobinRule.addServer(new Server("3", 80));
        ServerChooseTest.singleThread(roundRobinRule);
//        ServerChooseTest.multiThread(roundRobinRule);
    }

    @Override
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
