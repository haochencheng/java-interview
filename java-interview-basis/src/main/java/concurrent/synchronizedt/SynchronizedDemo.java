package concurrent.synchronizedt;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-08-12 12:59
 **/
public class SynchronizedDemo {

    private int a = 1;

    /**
     * 同步方法快
     */
    public synchronized void get1() throws InterruptedException {
        System.out.println("begin");
        //让出cpu 调度权 不释放 锁
        Thread.sleep(1);
        // 释放锁 进入同步队列 等待唤醒 如果在 get2中不 唤醒 将hang住
        // 注释掉 get2中 将打印 2
//        this.wait();
        System.out.println("after");
        timeout(1000);
        a = 2;
    }

    private void timeout(long timeout) {
        long l = System.currentTimeMillis();
        long l1 = l + timeout;
        while (System.currentTimeMillis() < l1) {

        }
    }

    /**
     * 同步方法快
     */
    public synchronized void get2() throws InterruptedException {
//        this.notify();
        printA();
    }


    public void getSyncClass() {
        synchronized (SynchronizedDemo.class){
            printA();
            a=3;
        }
    }

    public void getSyncString() {
        synchronized (""){
            printA();
            timeout(500);
            a=4;
        }
    }

    private void printA() {
        System.out.println(Thread.currentThread().getName()+a);
    }

    public void getSyncString1() {
        synchronized (""){
            printA();
        }
    }




    public static void main(String[] args) {
        // ============同步 同一个对象 开始=========== //
        //synchronized 效率低 多个方法同步 一个对象 会阻塞
        // 对象锁、类锁、字符串 锁的不是同一个对象
        // Thread.sleep(1); 让出cpu调度权 但并不释放锁
        // wait() 等待释放锁 等待唤醒 进入同步队列

        SynchronizedDemo synchronizedDemo = new SynchronizedDemo();
        Thread thread = new Thread(() -> {
            try {
                synchronizedDemo.get1();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.setName("get1-");
        Thread thread1 = new Thread(() -> {
            try {
                synchronizedDemo.get2();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread1.setName("get2-");
        Thread thread2 = new Thread(() -> synchronizedDemo.getSyncClass());
        thread2.setName("getSyncClass-");
        Thread thread3 = new Thread(() -> synchronizedDemo.getSyncString());
        thread3.setName("getSyncString-");
        Thread thread4 = new Thread(() -> synchronizedDemo.getSyncString1());
        thread4.setName("getSyncString1-");
        thread.start();
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        // ============同步 同一个对象 结束=========== //

    }


}
