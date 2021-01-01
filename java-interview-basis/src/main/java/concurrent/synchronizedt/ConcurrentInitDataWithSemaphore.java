package concurrent.synchronizedt;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * 并发初始化数据 只初始化一次
 * @description:
 * @author: haochencheng
 * @create: 2019-12-20 12:33
 **/
public class ConcurrentInitDataWithSemaphore {


    public static final int COUNT = 10;

    public static void main(String[] args) {
        CountDownLatch countDownLatch=new CountDownLatch(COUNT);
        ConcurrentInitDataWithSemaphore concurrentInitDataWithSemaphore =new ConcurrentInitDataWithSemaphore();
        for (int i = 0; i < COUNT; i++) {
            Thread thread = new Thread(new InitClass(concurrentInitDataWithSemaphore));
            thread.setName("线程-" + i);
            countDownLatch.countDown();
            thread.start();
        }
    }

    static class InitClass implements Runnable {

        private ConcurrentInitDataWithSemaphore concurrentInitDataWithSemaphore;

        public InitClass(ConcurrentInitDataWithSemaphore concurrentInitDataWithSemaphore) {
            this.concurrentInitDataWithSemaphore = concurrentInitDataWithSemaphore;
        }

        @Override
        public void run() {
            concurrentInitDataWithSemaphore.init();
        }
    }

    private Semaphore semaphore=new Semaphore(1);

    private void init(){
        System.out.println(Thread.currentThread().getName()+"准备执行");
        if (semaphore.availablePermits()==0){
            return;
        }
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName()+"获取许可成功");
        System.out.println("init");
    }

}
