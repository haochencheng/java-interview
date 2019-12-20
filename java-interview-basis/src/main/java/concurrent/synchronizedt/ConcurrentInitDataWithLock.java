package concurrent.synchronizedt;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 并发初始化数据 只初始化一次
 * @description:
 * @author: haochencheng
 * @create: 2019-12-20 12:33
 **/
public class ConcurrentInitDataWithLock {


    public static final int COUNT = 10;

    public static void main(String[] args) {
        CountDownLatch countDownLatch=new CountDownLatch(COUNT);
        ConcurrentInitDataWithLock concurrentInitDataWithSemaphore =new ConcurrentInitDataWithLock();
        for (int i = 0; i < COUNT; i++) {
            Thread thread = new Thread(new InitClass(concurrentInitDataWithSemaphore));
            thread.setName("线程-" + i);
            countDownLatch.countDown();
            thread.start();
        }
    }

    static class InitClass implements Runnable {

        private ConcurrentInitDataWithLock concurrentInitDataWithSemaphore;

        public InitClass(ConcurrentInitDataWithLock concurrentInitDataWithSemaphore) {
            this.concurrentInitDataWithSemaphore = concurrentInitDataWithSemaphore;
        }

        @Override
        public void run() {
            concurrentInitDataWithSemaphore.init();
        }
    }

    private Lock lock=new ReentrantLock();

    private void init(){
        System.out.println(Thread.currentThread().getName()+"准备执行");
        if (lock.tryLock()){
            System.out.println(Thread.currentThread().getName()+"获取许可成功");
            System.out.println("init");
        }
    }

}
