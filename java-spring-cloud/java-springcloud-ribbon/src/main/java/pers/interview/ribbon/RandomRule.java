package pers.interview.ribbon;

import pers.interview.ribbon.common.IRule;
import pers.interview.ribbon.common.Server;
import pers.interview.ribbon.util.ServerChooseTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 随机轮训规则
 *
 * @description:
 * @author: haochencheng
 * @create: 2019-09-11 18:36
 **/
public class RandomRule implements IRule {

    private List<Server> upServerList;

    private Random rand;



    public RandomRule() {
        rand = new Random();
        upServerList = Collections.synchronizedList(new ArrayList<>());
    }

    public static void main(String[] args) throws InterruptedException {
        RandomRule randomRule=new RandomRule();
        randomRule.addServer(new Server("1", 80));
        randomRule.addServer(new Server("2", 80));
        randomRule.addServer(new Server("3", 80));
        ServerChooseTest.singleThread(randomRule);
//        ServerChooseTest.multiThread(randomRule);
    }



    @Override
    public Server choose() {
        Server server = null;
        while (server == null) {
            if (Thread.interrupted()) {
                return null;
            }
            int serverCount = upServerList.size();
            if (serverCount == 0) {
                return null;
            }
            int index = rand.nextInt(serverCount);
            server = upServerList.get(index);
            if (server == null) {
                Thread.yield();
                continue;
            }
            Thread.yield();
        }
        return server;
    }

    private void addServer(Server server){
        upServerList.add(server);
    }


}
