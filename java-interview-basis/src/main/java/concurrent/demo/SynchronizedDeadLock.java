package concurrent.demo;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-12-19 11:22
 **/
public class SynchronizedDeadLock {

    public static void main(String[] args) {
        Runnable runnable1 = () -> {
            try {
                testA();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Runnable runnable2 = () -> {
            try {
                testB();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable1.run();
        runnable2.run();
    }

    private static synchronized void  testA() throws InterruptedException {
        Thread.sleep(1000);
        System.out.println("a");
    }

    private static synchronized void  testB() throws InterruptedException {
        System.out.println("b");
        Thread.sleep(1000);
        testB();
    }

}
