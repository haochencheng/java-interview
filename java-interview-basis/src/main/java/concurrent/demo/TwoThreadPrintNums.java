package concurrent.demo;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 场景：两个线程，一个线程打印基数，一个线程打印偶数。轮流打印
 *
 * @description:
 * @author: haochencheng
 * @create: 2019-12-19 10:26
 **/
public class TwoThreadPrintNums {

    public static final int LIMIT = 10;
    private static volatile Integer i = 0;

    public static void main(String[] args) {
        Runnable runnable1 = () -> {
            synchronized (i) {
                while (i < LIMIT) {
                    i++;
                    System.out.println(i);
                }
            }

        };
        Runnable runnable2 = () -> {
            synchronized (i) {
                while (i < LIMIT) {
                    i++;
                    System.out.println(i);
                }
            }
        };
        runnable1.run();
        runnable2.run();
    }

}
