package future;

import sun.misc.Unsafe;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.LockSupport;

/**
 * FeatureTask 实现原理
 * @param <T>
 */
public class FeatureCall<T> implements Runnable{


    public FeatureCall(Callable<T> callable) {
        this.callable = callable;
        this.waiter = Thread.currentThread();
    }

    public T result;
    private Callable<T> callable;
    private volatile int state=0;
    private Thread waiter;
    public static final int COMPLETE=1;

    public T get(){
        if (state!=COMPLETE){
            LockSupport.park(this);
        }
        return result;
    }

    @Override
    public void run() {
        try {
            result = callable.call();
            state=COMPLETE;
            LockSupport.unpark(waiter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
