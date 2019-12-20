package concurrent.synchronizedt;

import java.util.concurrent.CountDownLatch;

/**
 * 并发初始化数据 只初始化一次
 * @description:
 * @author: haochencheng
 * @create: 2019-12-20 12:33
 **/
public class ConcurrentInitDataWithVolatile {


    public static final int COUNT = 10;

    public static void main(String[] args) {
        CountDownLatch countDownLatch=new CountDownLatch(COUNT);
        ConcurrentInitDataWithVolatile concurrentInitDataWithSemaphore =new ConcurrentInitDataWithVolatile();
        for (int i = 0; i < COUNT; i++) {
            Thread thread = new Thread(new InitClass(concurrentInitDataWithSemaphore));
            thread.setName("线程-" + i);
            countDownLatch.countDown();
            thread.start();
        }
    }

    static class InitClass implements Runnable {

        private ConcurrentInitDataWithVolatile concurrentInitDataWithSemaphore;

        public InitClass(ConcurrentInitDataWithVolatile concurrentInitDataWithSemaphore) {
            this.concurrentInitDataWithSemaphore = concurrentInitDataWithSemaphore;
        }

        @Override
        public void run() {
            concurrentInitDataWithSemaphore.init();
        }
    }

    private volatile boolean init=false;

    private void init(){
        System.out.println(Thread.currentThread().getName()+"准备执行");
        if (init==true){
            return;
        }
        init=true;
        System.out.println(Thread.currentThread().getName()+"获取许可成功");
        System.out.println("init");
    }

}
