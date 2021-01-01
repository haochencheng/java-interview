package concurrent.countdownlatch;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * 跑步比赛 10位运动员参加比赛 裁判等所有运动员到齐 宣布比赛开始 运动员进行比赛 所有运动员跑完后 比赛结束
 * @description:
 * @author: haochencheng
 * @create: 2019-12-20 12:53
 **/
public class RunRaceCycleBarrier {

    public static final int COUNT = 11;
    private CountDownLatch beginCountDownLatch=new CountDownLatch(1);
    private CyclicBarrier cyclicBarrier=new CyclicBarrier(COUNT);

    public static void main(String[] args) {
        RunRaceCycleBarrier race=new RunRaceCycleBarrier();
        for (int i = 0; i < COUNT-1; i++) {
            Thread thread=new Thread(new Runner(race));
            thread.setName("线程-"+i);
            thread.start();
        }
        race.start();
        race.allEnd();
    }

    static class Runner implements Runnable{

        private RunRaceCycleBarrier runRace;

        public Runner(RunRaceCycleBarrier runRace) {
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
        try {
            cyclicBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        try {
            cyclicBarrier.await();
            System.out.println("所有选手已准备");
            System.out.println("比赛即将开始，倒计时");
            for (int i = 5 ; i >=0 ; i--) {
                System.out.println(i);
                Thread.sleep(1000);
            }
            System.out.println("比赛开始");
            cyclicBarrier.reset();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        } finally {
            beginCountDownLatch.countDown();
        }

    }

    private void end(long begin){
        System.out.println(Thread.currentThread().getName()+"跑步完成，成绩："+(System.currentTimeMillis()-begin));
        try {
            cyclicBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    private void allEnd(){
        try {
            cyclicBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }

        System.out.println("跑步完成,比赛圆满结束");
    }


}
