package concurrent.countdownlatch;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * 跑步比赛 10位运动员参加比赛 裁判等所有运动员到齐 宣布比赛开始 运动员进行比赛 所有运动员跑完后 比赛结束
 * @description:
 * @author: haochencheng
 * @create: 2019-12-20 12:53
 **/
public class RunRace {

    public static final int COUNT = 10;
    private CountDownLatch readyCountDownLatch=new CountDownLatch(COUNT);
    private CountDownLatch endCountDownLatch=new CountDownLatch(COUNT);
    private CountDownLatch beginCountDownLatch=new CountDownLatch(1);

    public static void main(String[] args) {
        RunRace race=new RunRace();
        for (int i = 0; i < COUNT; i++) {
            Thread thread=new Thread(new Runner(race));
            thread.setName("线程-"+i);
            thread.start();
        }
        try {
            race.readyCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        race.start();
        race.allEnd();
    }

    static class Runner implements Runnable{

        private RunRace runRace;

        public Runner(RunRace runRace) {
            this.runRace = runRace;
        }

        @Override
        public void run() {
            runRace.ready();
            try {
                runRace.beginCountDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long begin = System.currentTimeMillis();
            try {
                Thread.sleep(new Random().nextInt(1500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runRace.end(begin);
        }
    }

    private void ready(){
        try {
            Thread.sleep(new Random().nextInt(1500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName()+"已准备");
        readyCountDownLatch.countDown();
    }

    public void start(){
        try {
            readyCountDownLatch.await();
            System.out.println("所有选手已准备");
            System.out.println("比赛即将开始，倒计时");
            for (int i = 5 ; i >=0 ; i--) {
                System.out.println(i);
                Thread.sleep(1000);
            }
            System.out.println("比赛开始");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            beginCountDownLatch.countDown();
        }

    }

    private void end(long begin){
        System.out.println(Thread.currentThread().getName()+"跑步完成，成绩："+(System.currentTimeMillis()-begin));
        endCountDownLatch.countDown();
    }

    private void allEnd(){
        try {
            endCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("跑步完成,比赛圆满结束");
    }


}
