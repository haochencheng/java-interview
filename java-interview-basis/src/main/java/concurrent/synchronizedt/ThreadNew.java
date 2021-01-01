package concurrent.synchronizedt;


import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-08-13 23:40
 **/
public class ThreadNew {

    private int id;

    public ThreadNew(){
        this.id=nextThreadNum();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * 是否被挂起
     */
    private boolean isBusy=false;
    private static AtomicInteger threadInitNumber = new AtomicInteger();

    private static synchronized int nextThreadNum() {
        return threadInitNumber.getAndIncrement();
    }


    public void run() throws InterruptedException {
        //如果ObjectMonitor 的 owner为null 则共享资源无锁 当前线程获取锁
        do {
            //模拟阻塞
            Thread.sleep(100);
        }while (!ObjectMonitor.sync(this));
        //获取锁
        nextThreadNum();
        System.out.println(this.id);
        if (this.id % 2 == 0) {
            isBusy=true;
        }
        //模拟线程持有锁
        Thread.sleep(new Random().nextInt(2000));
        ObjectMonitor.release(this);
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }
}
