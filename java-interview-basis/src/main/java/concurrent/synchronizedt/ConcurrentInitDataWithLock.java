package concurrent.synchronizedt;

import java.util.concurrent.CountDownLatch;
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
            Thread thread = new Thread(new InitClass(concurrentInitDataWithSemaphore,countDownLatch));
            thread.setName("线程-" + i);
            thread.start();
        }
    }

    static class InitClass implements Runnable {

        private ConcurrentInitDataWithLock concurrentInitDataWithSemaphore;

        private CountDownLatch countDownLatch;

        public InitClass(ConcurrentInitDataWithLock concurrentInitDataWithSemaphore, CountDownLatch countDownLatch) {
            this.concurrentInitDataWithSemaphore = concurrentInitDataWithSemaphore;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            countDownLatch.countDown();
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            concurrentInitDataWithSemaphore.init();
        }
    }

    private ReentrantLock lock=new ReentrantLock();
    private volatile boolean init=false;

    private void init(){
        System.out.println(Thread.currentThread().getName()+"准备执行");
        if (lock.tryLock() && !init){
            System.out.println(Thread.currentThread().getName()+"获取许可成功");
            System.out.println("init");
            init=true;
        }
    }

}
