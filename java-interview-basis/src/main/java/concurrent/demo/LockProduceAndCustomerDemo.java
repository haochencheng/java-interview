package concurrent.demo;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * lock 生产者消费者 模型
 * @description:
 * @author: haochencheng
 * @create: 2019-12-19 16:16
 **/
public class LockProduceAndCustomerDemo {

    private Lock lock=new ReentrantLock();

    private LinkedList<Integer> list=new LinkedList<>();

    public static void main(String[] args) {
        LockProduceAndCustomerDemo lockProduceAndCustomerDemo=new LockProduceAndCustomerDemo();
        lockProduceAndCustomerDemo.run1(lockProduceAndCustomerDemo);
        lockProduceAndCustomerDemo.run2(lockProduceAndCustomerDemo);
    }

    private void run1(LockProduceAndCustomerDemo lockProduceAndCustomerDemo) {
        Thread thread1 = new Thread("A") {
            @Override
            public void run() {
                while (true){
                    lockProduceAndCustomerDemo.read();
                    try {
                        Thread.sleep(new Random().nextInt(2000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        thread1.start();
    }

    private void run2(LockProduceAndCustomerDemo lockProduceAndCustomerDemo) {
        Thread thread2 = new Thread("B") {
            @Override
            public void run() {
                while (true){
                    lockProduceAndCustomerDemo.write();
                    try {
                        Thread.sleep(new Random().nextInt(2000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread2.start();
    }

    public void read(){
        try {
            lock.lock();
            if (list.isEmpty()){
                System.out.println(Thread.currentThread().getName()+"消费者等待");
            }else {
                System.out.println(list.pop());
            }
        }finally {
            lock.unlock();
        }
    }

    public void write(){
        try {
            lock.lock();
            if (list.isEmpty()){
                list.add(new Random().nextInt(100));
            }else {
                System.out.println(Thread.currentThread().getName()+"生产者等待");
            }
        }finally {
            lock.unlock();
        }


    }

}
