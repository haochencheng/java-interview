package concurrent.demo.leetcode;

import sun.misc.Unsafe;

import java.util.concurrent.Semaphore;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-12-19 18:03
 **/
public class FooBar {

    private int n;

    public FooBar(int n) {
        this.n = n;
    }

    private Semaphore semaphore=new Semaphore(0);
    private Semaphore semaphore1=new Semaphore(1);

    public void foo(Runnable printFoo) throws InterruptedException {

        for (int i = 0; i < n; i++) {
            semaphore1.acquire();
            // printFoo.run() outputs "foo". Do not change or remove this line.
            printFoo.run();
            semaphore.release();
        }
    }

    public void bar(Runnable printBar) throws InterruptedException {

        for (int i = 0; i < n; i++) {
            semaphore.acquire();
            // printBar.run() outputs "bar". Do not change or remove this line.
            printBar.run();
            semaphore1.release();
        }
    }

}
