### java并发
#####   cpu调度模型
- 时间分片
    cpu为所有线程分配相同的调度时间
- 线程抢占
    java采用线程抢占的方式，如果当前线程执行完成或者让出cpu时间
    cpu判断是否有高优先级的线程，优先级相同随机调度

#####   进程和线程
进程是分配计算机资源的最小单位
线程是计算机调度的最小单元

一个线程是一个轻量级进程，同一进程中的所有线程共享进程资源。


####    JMM java内存模型
Java虚拟机规范中定义了Java内存模型（Java Memory Model，JMM），
用于屏蔽掉各种硬件和操作系统的内存访问差异，
以实现让Java程序在各种平台下都能达到一致的并发效果，
JMM规范了Java虚拟机与计算机内存是如何协同工作的：
规定了一个线程如何和何时可以看到由其他线程修改过后的共享变量的值，
以及在必须时如何同步的访问共享变量。

不同线程共享主存，主存中存放类信息

####    java线程通讯



####    synchronized
**无锁 VS 偏向锁 VS 轻量级锁 VS 重量级锁**
这四种锁是指锁的状态，专门针对synchronized的

Synchronize（监视器锁）
应用场景 分为 类锁/字符串锁/静态变量/静态方法 （全局锁）、对象锁（部分锁）
Synchronize通过java对象头中（Mark word）和Monitor(监视器) 实现线程同步
在JVM中，对象在内存中的布局分为三块区域：对象头、实例数据和对齐填充。如下：

实例数据：存放类的属性数据信息，包括父类的属性信息，如果是数组的实例部分还包括数组的长度，这部分内存按4字节对齐。

#####   Java对象头
synchronized使用的锁对象是存储在Java对象头里的，jvm中采用2个字来存储对象头
(如果对象是数组则会分配3个字，多出来的1个字记录的是数组长度)，
其主要结构是由Mark Word 和 Class Metadata Address 组成，其结构说明如下表：

| 虚拟机位数  |	头对象结构  |	说明 |    
| :------| ------: | :------: |  
32/64bit  |	Mark Word  |	存储对象的hashCode、锁信息或分代年龄或GC标志等信息 |
32/64bit  |	Class Metadata Address |	类型指针指向对象的类元数据，JVM通过这个指针确定该对象是哪个类的实例。|

| 锁状态	| 25bit	| 4bit	| 1bit是否是偏向锁  |	2bit 锁标志位 |
| :------| ------: | :------: |   :------: | :------: |
|无锁状态 |	对象HashCode	| 对象分代年龄 |	0 |	01


- 偏向锁

- 轻量级锁

- 重量级锁

####    java主流锁
![java主流锁](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/java-basis/concurrent/java%E4%B8%BB%E6%B5%81%E9%94%81.png)

### CAS cpu源语 cmpxchg指令
CAS全称 Compare And Swap（比较与交换），是一种无锁算法
在不使用锁（没有线程被阻塞）的情况下实现多线程之间的变量同步。
java.util.concurrent包中的原子类就是通过CAS来实现了乐观锁。

CAS算法涉及到三个操作数：

- 需要读写的内存值 V。
- 进行比较的值 A。
- 要写入的新值 B。

根据定义我们可以看出各属性的作用：

unsafe： 获取并操作内存的数据。
valueOffset： 存储value在AtomicInteger中的偏移量。
value： 存储AtomicInteger的int值，该属性需要借助volatile关键字保证其在线程间是可见的。


```java
public class AtomicInteger extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 6214790243416807050L;

    // setup to use Unsafe.compareAndSwapInt for updates
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;

    static {
        try {
            valueOffset = unsafe.objectFieldOffset
                (AtomicInteger.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    private volatile int value;
    
    
    //...
    
    public final int getAndIncrement() {
            return unsafe.getAndAddInt(this, valueOffset, 1);
    }


    //Unsafe.class ... 如果（线程工作内存/寄存器（L1,L2,L3缓存）中的）原值 与 内存中（java线程主存）不相同，即别的
    //线程修改了 共享变量，循环重新从内存 地址 中获取变量值 写入寄存器（工作内存），并在新值基础上 执行CAS操作。
     public final int getAndAddInt(Object var1, long var2, int var4) {
            int var5;
            do {
                var5 = this.getIntVolatile(var1, var2);
            } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));
    
            return var5;
    }
}

```

CAS存在的问题 
**ABA问题** 
CAS需要在操作值的时候检查内存值是否发生变化，没有发生变化才会更新内存值。但是如果内存值原来是A，后来变成了B，
然后又变成了A，那么CAS进行检查时会发现值没有发生变化，但是实际上是有变化的。ABA问题的解决思路就是在变量前面添加版本号，
每次变量更新的时候都把版本号加一，这样变化过程就从“A－B－A”变成了“1A－2B－3A”。

**循环时间长开销大**
CAS操作如果长时间不成功，会导致其一直自旋，给CPU带来非常大的开销。

####    volatile
volatile 保证了共享变量的线程可见性
volatile修饰的变量，发生修改后刷新到主存，强制其他线程在获取该变量时工作空间缓存无效直接从主存获取。

####   自旋锁 VS 适应性自旋锁









### 相关博客
https://tech.meituan.com/2018/11/15/java-lock.html
