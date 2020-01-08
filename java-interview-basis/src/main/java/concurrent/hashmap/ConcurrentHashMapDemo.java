package concurrent.hashmap;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @description:
 * @author: haochencheng
 * @create: 2020-01-07 18:20
 **/
public class ConcurrentHashMapDemo {


    private final static ConcurrentHashMap<String,String> hashMap=new ConcurrentHashMap(16);
    public static final int COUNT = 40;
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
        thread.join();
        Set<String> set = hashMap.keySet();
        int count=0;
        System.out.println(hashMap.size());
        if (count!=hashMap.size()){
            System.out.println("数据丢失"+(COUNT-hashMap.size()));
        }
        System.out.println(hashMap.get(""));
    }

}
