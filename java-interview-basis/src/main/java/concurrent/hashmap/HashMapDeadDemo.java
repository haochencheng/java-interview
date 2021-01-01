package concurrent.hashmap;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * 模拟并发条件下hashmap 死循环
 * @description:
 * @author: haochencheng
 * @create: 2020-01-07 16:41
 **/
public class HashMapDeadDemo {

    private final static HashMap<String,String> hashMap=new HashMap(2);
    public static final int COUNT = 1000;
    private static final CountDownLatch countDownLatch=new CountDownLatch(COUNT);

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(() -> {
            for (int i = 0; i < COUNT; i++) {
                Thread thread1 = new Thread(() -> {
                    String uuid = UUID.randomUUID().toString();
                    countDownLatch.countDown();
                    hashMap.put(uuid, uuid);
                });
                thread1.start();
            }
        });
        thread.start();
        countDownLatch.await();
        int count=0;
        System.out.println(hashMap.size());
        // 可能数据丢失
        if (count!=hashMap.size()){
            System.out.println("数据丢失"+(COUNT-hashMap.size()));
        }
    }


}
