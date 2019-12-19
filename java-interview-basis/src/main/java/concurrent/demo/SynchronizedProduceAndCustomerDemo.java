package concurrent.demo;

import java.util.LinkedList;
import java.util.Random;

/**
 * synchronized 生产者消费者 模型
 * @description:
 * @author: haochencheng
 * @create: 2019-12-19 15:02
 **/
public class SynchronizedProduceAndCustomerDemo {

    private LinkedList<Integer> list = new LinkedList<>();

    public static void main(String[] args) {
        SynchronizedProduceAndCustomerDemo synchronizedProduceAndCustomerDemo = new SynchronizedProduceAndCustomerDemo();
        synchronizedProduceAndCustomerDemo.run1(synchronizedProduceAndCustomerDemo);
        synchronizedProduceAndCustomerDemo.run2(synchronizedProduceAndCustomerDemo);
    }

    private void run2(SynchronizedProduceAndCustomerDemo synchronizedProduceAndCustomerDemo) {
        Thread thread2 = new Thread("B") {
            @Override
            public void run() {
                while (true){
                    synchronizedProduceAndCustomerDemo.read();
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        thread2.start();
    }

    private void run1(SynchronizedProduceAndCustomerDemo synchronizedProduceAndCustomerDemo) {
        Thread thread1 = new Thread("A") {
            @Override
            public void run() {
                while (true){
                    synchronizedProduceAndCustomerDemo.write();
                    try {
                        sleep(1200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        thread1.start();
    }

    private void read(){
        synchronized (this) {
            System.out.println(Thread.currentThread().getName() + "线程获取锁成功");
            if (list.isEmpty()) {
                System.out.println(Thread.currentThread().getName() + "============等待获取资源");
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                System.out.println(list.pop());
                this.notify();
            }
        }
    }

    public void write(){
        synchronized (this) {
            // 程序调用 Thread.sleep()  Thread.yield() 这些方法暂停线程的执行，不会释放。
//                        try {
//                            Thread.sleep(5000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        Thread.yield();
            // 同步对象 wait
            System.out.println(Thread.currentThread().getName() + "线程获取锁成功");
            if (!list.isEmpty()) {
                try {
                    System.out.println(Thread.currentThread().getName()+ "等待消费者消费");
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                list.add(new Random().nextInt(10));
                list.add(new Random().nextInt(10));
                this.notify();
            }
        }
    }

}
