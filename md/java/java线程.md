####    线程状态及状体转换
1. New - 新建
2. Runable - 可运行
3. Blocked - 阻塞的 
4. Waiting - 无限期等待 
    - java.lang.Object#wait();
    - threadObj.join()/threadObj.join(0)方法
    - LockSupport.park()方法
5. Timed Waiting - 期限等待 
    在一定时间内会被系统自动唤醒，当然也会被唤醒。调用如下方法会让线程进入期限等待状态：

    - java.lang.Object#wait(long);
    - java.lang.Thread#sleep(long);
    - threadObj.join(n)方法
    - LockSupport.parkNanos(obj ,n)方法
    - LockSupport.parkUntil(obj ,n)方法
    
6. Terminated 
    已经终止的线程处于这种状态

![image](https://static.oschina.net/uploads/space/2013/0621/174442_0BNr_182175.jpg)

### ReentrantLock重入锁
一个可重入的互斥锁定 Lock，它具有与使用 synchronized 方法和语句所访问的隐式监视器锁定相同的一些基本行为和语义，但功能更强大。ReentrantLock 将由最近成功获得锁定，并且还没有释放该锁定的线程所拥有。当锁定没有被另一个线程所拥有时，调用 lock 的线程将成功获取该锁定并返回。如果当前线程已经拥有该锁定，此方法将立即返回。可以使用 isHeldByCurrentThread() 和 getHoldCount() 方法来检查此情况是否发生。

ReentrantLock实现Lock接口       
抽象静态内部类Sync继承抽象同步队列AbstractQueuedSynchronizer

2个静态内部类继承了Sync
- NonfairSync    (non-fair locks)非公平锁
- FairSync  (fair locks)公平锁


```
abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -5179523762034025860L;

        /**
         * Performs {@link Lock#lock}. The main reason for subclassing
         * is to allow fast path for nonfair version.
         */
        abstract void lock();

        /**
         * Performs non-fair tryLock.  tryAcquire is implemented in
         * subclasses, but both need nonfair try for trylock method.
         */
        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }

        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }

        protected final boolean isHeldExclusively() {
            // While we must in general read state before owner,
            // we don't need to do so to check if current thread is owner
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        // Methods relayed from outer class

        final Thread getOwner() {
            return getState() == 0 ? null : getExclusiveOwnerThread();
        }

        final int getHoldCount() {
            return isHeldExclusively() ? getState() : 0;
        }

        final boolean isLocked() {
            return getState() != 0;
        }

        /**
         * Reconstitutes the instance from a stream (that is, deserializes it).
         */
        private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            setState(0); // reset to unlocked state
        }
    }

static final class NonfairSync extends Sync {
    private static final long serialVersionUID = 7316153563782823691L;

    /**
     * Performs lock.  Try immediate barge, backing up to normal
     * acquire on failure.
     */
    final void lock() {
        if (compareAndSetState(0, 1))
            setExclusiveOwnerThread(Thread.currentThread());
        else
            acquire(1);
    }

    protected final boolean tryAcquire(int acquires) {
        return nonfairTryAcquire(acquires);
    }
}
```


```
static final class FairSync extends Sync {
    private static final long serialVersionUID = -3000897897090466540L;

    final void lock() {
        acquire(1);
    }

    /**
     * Fair version of tryAcquire.  Don't grant access unless
     * recursive call or no waiters or is first.
     */
    protected final boolean tryAcquire(int acquires) {
        final Thread current = Thread.currentThread();
        int c = getState();     //获取同步状态
        if (c == 0) {   //无锁cas设置状态
            if (!hasQueuedPredecessors() &&
                compareAndSetState(0, acquires)) {
                setExclusiveOwnerThread(current); //设置当前线程为锁拥有者
                return true;
            }
        }
        else if (current == getExclusiveOwnerThread()) {    
        //如果当前线程已经获取锁，同步状态+1
            int nextc = c + acquires;
            if (nextc < 0)
                throw new Error("Maximum lock count exceeded");
            setState(nextc);
            return true;
        }
        return false;
    }
}
```

####    java多线程实现的4种方式
Java多线程实现方式主要有四种：继承Thread类、实现Runnable接口、实现Callable接口通过FutureTask包装器来创建Thread线程、使用ExecutorService、Callable、Future实现有返回结果的多线程

####    Runnable接口
函数式接口(Functional Interface)是Java 8对一类特殊类型的接口的称呼。 这类接口只定义了唯一的抽象方法的接口（除了隐含的Object对象的公共方法）， 因此最开始也就做SAM类型的接口（Single Abstract Method）。

为什么会单单从接口中定义出此类接口呢？ 原因是在Java Lambda的实现中， 开发组不想再为Lambda表达式单独定义一种特殊的Structural函数类型，称之为箭头类型（arrow type）， 依然想采用Java既有的类型系统(class, interface, method等)， 原因是增加一个结构化的函数类型会增加函数类型的复杂性，破坏既有的Java类型，并对成千上万的Java类库造成严重的影响。 权衡利弊， 因此最终还是利用SAM 接口作为 Lambda表达式的目标类型。

实现runnable接口的函数会创建一个thread
```
@FunctionalInterface
public interface Runnable {
    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see     java.lang.Thread#run()
     */
    public abstract void run();
    
    //初始化线程
    public Thread() {
        init(null, null, "Thread-" + nextThreadNum(), 0);
    }
    
    //构造器，设置Thread属性 ThreadGroup、Runnable、name、stackSize
    
    public Thread(Runnable target) {
        init(null, target, "Thread-" + nextThreadNum(), 0);
    }
    
     public Thread(ThreadGroup group, Runnable target, String name) {
        init(group, target, name, 0);
    }


    //启动线程
    public synchronized void start() {
        /**
         * This method is not invoked for the main method thread or "system"
         * group threads created/set up by the VM. Any new functionality added
         * to this method in the future may have to also be added to the VM.
         *
         * A zero status value corresponds to state "NEW".
         */
        if (threadStatus != 0)
            throw new IllegalThreadStateException();

        /* Notify the group that this thread is about to be started
         * so that it can be added to the group's list of threads
         * and the group's unstarted count can be decremented. */
        group.add(this);

        boolean started = false;
        try {
            start0();
            started = true;
        } finally {
            try {
                if (!started) {
                    group.threadStartFailed(this);
                }
            } catch (Throwable ignore) {
                /* do nothing. If start0 threw a Throwable then
                  it will be passed up the call stack */
            }
        }
    }

}
```

####    Thread类
Thread类实现Runnable接口
```
public
class Thread implements Runnable {

    //Thread线程名字
    private volatile String name;
    //线程优先级
    private int            priority;
    //线程实例
    private Thread         threadQ;
    
    private long           eetop;

    /* Whether or not to single_step this thread. */
    private boolean     single_step;

    /* Whether or not the thread is a daemon thread. */
    private boolean     daemon = false;

    /* JVM state */
    private boolean     stillborn = false;

    /* What will be run. 将会被执行的任务*/
    private Runnable target;

    /* The group of this thread 线程组*/
    private ThreadGroup group;

    /* The context ClassLoader for this thread 加载这个类的类加载器*/
    private ClassLoader contextClassLoader;

    /* The inherited AccessControlContext of this thread */
    private AccessControlContext inheritedAccessControlContext;

    /* For autonumbering anonymous threads. */
    private static int threadInitNumber;
    private static synchronized int nextThreadNum() {
        return threadInitNumber++;
    }
    
    /*
     * Thread ID 线程id
     */
    private long tid;
        
    //线程优先级    
     /**
     * The minimum priority that a thread can have.
     */
    public final static int MIN_PRIORITY = 1;

   /**
     * The default priority that is assigned to a thread.
     */
    public final static int NORM_PRIORITY = 5;

    /**
     * The maximum priority that a thread can have.
     */
    public final static int MAX_PRIORITY = 10;
    
    //获取当前线程
    public static native Thread currentThread();
    
    //让渡当前线程资源
    public static native void yield();
    
    //休眠
    public static void sleep(long millis, int nanos)
    throws InterruptedException {
        if (millis < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }

        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException(
                                "nanosecond timeout value out of range");
        }

        if (nanos >= 500000 || (nanos != 0 && millis == 0)) {
            millis++;
        }

        sleep(millis);
    }
    
    public enum State {
        /**
         * Thread state for a thread which has not yet started.
         */
        NEW,

        /**
         * Thread state for a runnable thread.  A thread in the runnable
         * state is executing in the Java virtual machine but it may
         * be waiting for other resources from the operating system
         * such as processor.
         */
        RUNNABLE,

        /**
         * Thread state for a thread blocked waiting for a monitor lock.
         * A thread in the blocked state is waiting for a monitor lock
         * to enter a synchronized block/method or
         * reenter a synchronized block/method after calling
         * {@link Object#wait() Object.wait}.
         */
        BLOCKED,

        /**
         * Thread state for a waiting thread.
         * A thread is in the waiting state due to calling one of the
         * following methods:
         * <ul>
         *   <li>{@link Object#wait() Object.wait} with no timeout</li>
         *   <li>{@link #join() Thread.join} with no timeout</li>
         *   <li>{@link LockSupport#park() LockSupport.park}</li>
         * </ul>
         *
         * <p>A thread in the waiting state is waiting for another thread to
         * perform a particular action.
         *
         * For example, a thread that has called <tt>Object.wait()</tt>
         * on an object is waiting for another thread to call
         * <tt>Object.notify()</tt> or <tt>Object.notifyAll()</tt> on
         * that object. A thread that has called <tt>Thread.join()</tt>
         * is waiting for a specified thread to terminate.
         */
        WAITING,

        /**
         * Thread state for a waiting thread with a specified waiting time.
         * A thread is in the timed waiting state due to calling one of
         * the following methods with a specified positive waiting time:
         * <ul>
         *   <li>{@link #sleep Thread.sleep}</li>
         *   <li>{@link Object#wait(long) Object.wait} with timeout</li>
         *   <li>{@link #join(long) Thread.join} with timeout</li>
         *   <li>{@link LockSupport#parkNanos LockSupport.parkNanos}</li>
         *   <li>{@link LockSupport#parkUntil LockSupport.parkUntil}</li>
         * </ul>
         */
        TIMED_WAITING,

        /**
         * Thread state for a terminated thread.
         * The thread has completed execution.
         */
        TERMINATED;
    }
    
}
```
yield()方法
理论上，yield意味着放手，放弃，投降。一个调用yield()方法的线程告诉虚拟机它乐意让其他线程占用自己的位置。这表明该线程没有在做一些紧急的事情。注意，这仅是一个暗示，并不能保证不会产生任何影响。


join()方法
线程实例的方法join()方法可以使得一个线程在另一个线程结束后再执行。如果join()方法在一个线程实例上调用，当前运行着的线程将阻塞直到这个线程实例完成了执行。


###   ExecutorService
![image](http://images2015.cnblogs.com/blog/475531/201604/475531-20160406200908922-901179809.png)


####    Executor
An object that executes submitted {@link Runnable} tasks.
一个执行提交任务的对象


```
public interface Executor {

    /**
     * Executes the given command at some time in the future.  The command
     * may execute in a new thread, in a pooled thread, or in the calling
     * thread, at the discretion of the {@code Executor} implementation.
     *
     * @param command the runnable task
     * @throws RejectedExecutionException if this task cannot be
     * accepted for execution
     * @throws NullPointerException if command is null
     */
    void execute(Runnable command);
}
```


####    ExecutorService

```
    //启动一次顺序关闭，执行以前提交的任务，但不接受新任务。如果已经关闭，则调用没有其他作用。
    void shutdown();
    
    //尝试停止所有正在执行的任务，返回正在执行的任务列表
    List<Runnable> shutdownNow();

    //线程是否结束
    boolean isShutdown();
    
    //如果关闭后所有任务都已完成，则返回 true。注意，除非首先调用shutdown 或 shutdownNow，否则 isTerminated 永不为 true。
    boolean isTerminated();
    
    //请求关闭、发生超时或者当前线程中断，无论哪一个首先发生之后，都将导致阻塞，直到所有任务完成执行。
    boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException;

    //提交一个返回值的任务用于执行，返回一个表示任务的未决结果的 Future。该 Future 的 get 方法在成功完成时将会返回该任务的结果。
    如果想立即阻塞任务的等待，则可以使用 result = exec.submit(aCallable).get(); 形式的构造。
    <T> Future<T> submit(Callable<T> task);

    //提交一个 Runnable 任务用于执行，并返回一个表示该任务的 Future。该 Future 的 get 方法在成功完成时将会返回给定的结果。
    <T> Future<T> submit(Runnable task, T result) 

    //提交一个 Runnable 任务用于执行，并返回一个表示该任务的 Future。该 Future 的 get 方法在成功 完成时将会返回nul
    Future<?> submit(Runnable task)

    //执行给定的任务，当所有任务完成或超时期满时（无论哪个首先发生），返回保持任务状态和结果的 Future 列表。返回列表的所有元素的 Future.isDone() 为true。一旦返回后，即取消尚未完成的任务。注意，可以正常地或通过抛出异常来终止已完成 任务。如果此操作正在进行时修改了给定的 collection，则此方法的结果是不确定的。
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException

```

#### ThreadPoolExecutor

step1.调用ThreadPoolExecutor的execute提交线程，首先检查CorePool，如果CorePool内的线程小于CorePoolSize，新创建线程执行任务。
step2.如果当前CorePool内的线程大于等于CorePoolSize，那么将线程加入到BlockingQueue。
step3.如果不能加入BlockingQueue，在小于MaxPoolSize的情况下创建线程执行任务。
step4.如果线程数大于等于MaxPoolSize，那么执行拒绝策略。

![image](https://upload-images.jianshu.io/upload_images/2177145-33c7b5ff75cf2bf7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

###   Executors
使用 Executors 创建线程池

newCachedThreadPoo,newFixedThreadPool,newScheduledThreadPool,newSingleThreadExecutor 


#### newFixedThreadPool
由于使用了LinkedBlockingQueue所以maximumPoolSize没用，当corePoolSize满了之后就加入到LinkedBlockingQueue队列中。
每当某个线程执行完成之后就从LinkedBlockingQueue队列中取一个。
所以这个是创建固定大小的线程池。

```
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(
            nThreads,
            nThreads,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
}
```

####    newSingleThreadPool
创建线程数为1的线程池，由于使用了LinkedBlockingQueue所以maximumPoolSize 没用，corePoolSize为1表示线程数大小为1,满了就放入队列中，执行完了就从队列取一个。

```
public static ExecutorService newSingleThreadExecutor() {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>()));
    }
```

####    newCachedThreadPool
创建可缓冲的线程池。没有大小限制。由于corePoolSize为0所以任务会放入SynchronousQueue队列中，SynchronousQueue只能存放大小为1，所以会立刻新起线程，由于maxumumPoolSize为Integer.MAX_VALUE所以可以认为大小为2147483647。受内存大小限制。

```
  public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
    }
    
    
     public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), defaultHandler);
    }
    
```

####    

```
public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
        if (corePoolSize < 0 ||
            maximumPoolSize <= 0 ||
            maximumPoolSize < corePoolSize ||
            keepAliveTime < 0)
            throw new IllegalArgumentException();
        if (workQueue == null || threadFactory == null || handler == null)
            throw new NullPointerException();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
    }
```

####    构造函数参数

1、corePoolSize 核心线程数大小，当线程数 < corePoolSize ，会创建线程执行 runnable

2、maximumPoolSize 最大线程数， 当线程数 >= corePoolSize的时候，会把 runnable 放入 workQueue中

3、keepAliveTime 保持存活时间，当线程数大于corePoolSize的空闲线程能保持的最大时间。

4、unit 时间单位

5、workQueue 保存任务的阻塞队列

6、threadFactory 创建线程的工厂

7、handler 拒绝策略

####    任务执行顺序
1、当线程数小于 corePoolSize时，创建线程执行任务。

2、当线程数大于等于 corePoolSize并且 workQueue 没有满时，放入workQueue中

3、线程数大于等于 corePoolSize并且当 workQueue 满时，新任务新建线程运行，线程总数要小于 maximumPoolSize

4、当线程总数等于 maximumPoolSize 并且 workQueue 满了的时候执行 handler 的 rejectedExecution。也就是拒绝策略。

####    JDK7提供了7个阻塞队列。（也属于并发容器）

ArrayBlockingQueue ：一个由数组结构组成的有界阻塞队列。
LinkedBlockingQueue ：一个由链表结构组成的有界阻塞队列。
PriorityBlockingQueue ：一个支持优先级排序的无界阻塞队列。
DelayQueue：一个使用优先级队列实现的无界阻塞队列。
SynchronousQueue：一个不存储元素的阻塞队列。
LinkedTransferQueue：一个由链表结构组成的无界阻塞队列。
LinkedBlockingDeque：一个由链表结构组成的双向阻塞队列。

####    什么是阻塞队列？
阻塞队列是一个在队列基础上又支持了两个附加操作的队列。

2个附加操作：

支持阻塞的插入方法：队列满时，队列会阻塞插入元素的线程，直到队列不满。
支持阻塞的移除方法：队列空时，获取元素的线程会等待队列变为非空。

####    四个拒绝策略
ThreadPoolExecutor默认有四个拒绝策略：

1、ThreadPoolExecutor.AbortPolicy() 直接抛出异常RejectedExecutionException

2、ThreadPoolExecutor.CallerRunsPolicy() 直接调用run方法并且阻塞执行

3、ThreadPoolExecutor.DiscardPolicy() 直接丢弃后来的任务

4、ThreadPoolExecutor.DiscardOldestPolicy() 丢弃在队列中队首的任务

当然可以自己继承RejectedExecutionHandler来写拒绝策略.