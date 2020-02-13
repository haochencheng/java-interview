​                                                                                  

#	java调优

##	调优场景

调优先定位问题，性能瓶颈在哪里，找到慢接口具体问题具体分析。

因此，一个性能监控工具 APM 和 分布式链路系统 可以有效的帮助调优。

- 一个接口同步调用多个服务
- 数据库瓶颈
- redis瓶颈
- 服务器瓶颈
- jvm问题

###	一个接口同步调用多个服务

1. 同步调用接口没有依赖

​		可以用线程池并发执行任务，如果不需要获取返回值，可以使用消息队列 。

eg. A -> { B(1s) -> C(2s) -> D(3s) } = 6s

使用java Executors框架

​     A -{

​			-> B(1s) 

​			-> C(2s)

​			-> D(3s)

​		} = 3s

如果调用B 执行1s ，调用C执行2s，调用D执行3s 

那么同步的话总时间要花费6s

异步的话花费3s

```java

```



2. 同步调用接口有依赖

- 优化调用链路，减少响应时间
- 减少请求调用
- 同步服务异步化



#####	优化调用链路，减少响应时间

优化下游服务的响应时间，通过加缓存等

eg. A -> { B(1s) -> C(1s) -> D(1s) } = 3s

比之前6s 省了3s。

#####	减少请求调用次数

有的读者可能会问，有依赖怎么能减少请求调用呢？

这种场景比较特殊。就是可以在调用方加缓存的情况下，比如微信或者auth2中 的accessToken 返回的数据是有时效性的数据。

可以通过在调用方加缓存，减少调用次数来优化。

eg. B接口可以缓存 

​	  第一次  A -> { B(1s) -> C(2s) -> D(3s) } = 6s

​      第二次  A -> {  C(2s) -> D(3s) } = 5s

​       ... 缓存有效期内                       = 5s

注意问题：

​	可能存在热点key问题，缓存随机失效。



#####	同步服务异步化

​		

#####	Executors 框架



###	调用第三方接口

#####	批量调用立方门禁权限接口

线上环境，用户定会议室会调用立方门禁授权接口进行授权，有时效性。会议室按小时计费，如 2020-01-09 8:00 - 2020-01-09 8:00。

用户开门时调用立方获取权限接口，返回用户所有权限（包括过期记录）。随着时间增长，部分用户返回数据量大，高峰期部分接口响应慢。

优化：

1.去除用户过期权限

调用立方批量获取用户权限接口，如果权限过期，取消授权。

为了不增加立方接口压力，平缓调用，限制调用频率。

客户端采用阻塞队列和信号量来控制调用频率。

```java
private static class RemoveReformerTask {

        private final static LinkedBlockingQueue<MemberResult.OpenData> blockingQueue = new LinkedBlockingQueue(20);
        public static final int N_THREADS = 2;
        private final static Semaphore semaphore = new Semaphore(N_THREADS);
        private static final DateTimeFormatter dtf=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        private static AtomicBoolean syncFlag=new AtomicBoolean(true);

        public static void main(String[] args) {
            removeTask();
        }

        private static void removeTask() {
            cancel();
            // 分页批量获取用户权限
            MemberFormPage memberFormPage = new MemberFormPage();
            int page = 12436;
            int pageSize=40;
            int pageMax;
            memberFormPage.setPageSize(pageSize);
            do {
                System.out.println("当前页数："+page);
                memberFormPage.setPage(page);
                ReformerResult<MemberResult> reformerResult = getAllMember(memberFormPage);
                MemberResult memberResult = reformerResult.getData();
                List<MemberResult.OpenData> openHistoryList = memberResult.getOpenHistorys();
                if (CollectionUtils.isEmpty(openHistoryList)) {
                    System.out.println("同步记录为空，同步结束=================page:"+page);
                    break;
                }
                Integer totalSize = memberResult.getTotalSize();
                pageMax = totalSize / pageSize;
                List<MemberResult.OpenData> removeList = openHistoryList.stream().filter(openData -> {
                    String stopDate = openData.getStopDate();
                    if (Strings.isNullOrEmpty(stopDate)) {
                        return false;
                    }
                    try {
                        LocalDateTime stopDateTime = LocalDateTime.parse(stopDate, dtf);
                        return stopDateTime.isBefore(LocalDateTime.now());
                    }catch (Exception e){
                        return false;
                    }
                }).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(removeList)){
                    page++;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                for (MemberResult.OpenData openData : removeList) {
                    try {
                        blockingQueue.put(openData);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                page++;
            } while (page < pageMax);
            syncFlag.set(false);
            System.out.println("同步结束=================page:"+page);
        }

        private static void cancel() {
            for (int i = 0; i < N_THREADS; i++) {
                Runnable runnable = () -> cancelTask();
                Thread thread=new Thread(runnable);
                thread.start();
            }
        }

        private static void cancelTask() {
            while (syncFlag.get()){
                try {
                    semaphore.acquire();
                    MemberResult.OpenData openData = blockingQueue.take();
                   // 取消授权方法
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                }
            }
        }
    }
```



### JVM优化

#####		线上接口平均响应慢

​	 性能监控平台发现一台服务器频繁yanggc 且花费时间较长。

查看jvm内存分配发现form 27M，查看线上服务器发现内存不足。

本地机房，这台服务器上部署了redis，占用大量内存 。

更换服务器，线上集群三台,增加启动参数。

java分代垃圾回收。

- 青年代

  包括eden，survivor区。survivor又分为from和to区(s0,s1)。
  程序新创建的对象都是从新生代分配内存

- 老年代

  用于存放经过多次新生代GC任然存活的对象

  例如缓存对象

  老年代所占的内存大小为-Xmx对应的值减去-Xmn对应的值。

- 新建的对象也有可能直接进入老年代

  主要有两种情况：

  ①大对象，可通过启动参数设置-XX:PretenureSizeThreshold=1024(单位为字节，默认为0)来代表超过多大时就不在新生代分配，而是直接在老年代分配。

  ②大的数组对象，切数组中无引用外部对象。

#####	jvm参数介绍

- -Xms 

  java堆初始化内存大小（最小值）

  默认为操作系统物理内存的1/64但小于1G

- Xmx 

  java堆最大内存大小

  默认为物理内存的1/4但小于1G

- 动态扩容问题

  jvm运行时会根据堆内存使用占比动态调整堆内存大小。对于运行系统，为避免在运行时频繁调整Heap的大小，通常-Xms与-Xmx的值设成一样

- -XX:MinHeapFreeRation

  默认当空余堆内存小于40%时，JVM会增大Heap到-Xmx指定的大小

- -XX:MaxHeapFreeRation

  当空余堆内存大于70%时，JVM会减小heap的大小到-Xms指定的大小

- -Xmn

  新生代的大小

- -XX:SurvivorRation

  Eden Space及Survivor Space的占比，默认8:1:1

- -XX:MaxTenuringThreshold=15

  设置垃圾最大年龄

  如果设置为0的话，则年轻代对象不经过Survivor区，直接进入年老代。对于年老代比较多的应用，可以提高效率。

  如果将此值设置为一个较大值，则年轻代对象会在Survivor区进行多次复制，这样可以增加对象再年轻代的存活时间，增加在年轻代即被回收的概论。（吞吐量高）

- -XX:MaxDirectMemorySize=1G

  直接内存。报java.lang.OutOfMemoryError: Direct buffer memory 异常可以上调这个值。

- -XX:+DisableExplicitGC

  禁止运行期显式地调用 System.gc() 来触发fulll GC。

- -XX:CMSInitiatingOccupancyFraction=60

  老年代内存回收阈值，默认值为68。

- -XX:ConcGCThreads=4：

  CMS垃圾回收器并行线程线，推荐值为CPU核心数。

- -XX:ParallelGCThreads=8：

  新生代并行收集器的线程数。

- -XX:CMSFullGCsBeforeCompaction=4

  指定进行多少次fullGC之后，进行tenured区 内存空间压缩。

- -XX:CMSMaxAbortablePrecleanTime=500：

  当abortable-preclean预清理阶段执行达到这个时间时就会结束。

#####	回收器选择

​	JVM给了三种选择：串行收集器、并行收集器、并发收集器，但是串行收集器只适用于小数据量的情况，所以这里的选择主要针对并行收集器和并发收集器。

- 吞吐量优先的并行收集器
  如上文所述，并行收集器主要以到达一定的吞吐量为目标，适用于科学技术和后台处理等

  

###	数据库优化

#####	表数据量大

日志类型数据库，量大后慢。

1. 日志类型sql 异步。
2. 分库分表
3. 批量插入



#####	优化索引

```sql
explain 查看执行计划
```



#####	分库分表

