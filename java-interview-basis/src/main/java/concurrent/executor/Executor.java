package concurrent.executor;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: haochencheng
 * @create: 2020-02-25 22:12
 **/
public class Executor {

    public static void main(String[] args) {
//        normal();
        taskGtWorker();
    }

    private static void taskGtWorker(){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 2, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2));
        for (int i = 0; i < 10; i++) {
            threadPoolExecutor.execute(()->{
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(new Random().nextDouble());
            });
        }
        threadPoolExecutor.shutdown();
    }

    private static void normal() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
        for (int i = 0; i < 100; i++) {
            threadPoolExecutor.execute(()->{
                System.out.println(new Random().nextDouble());
            });
        }
        threadPoolExecutor.shutdown();
    }

}
