https://cloud.tencent.com/developer/article/1103077



https://www.lovesofttech.com/linux/centOSTomcatConfig.html



###	统计网络连接状态的命令

```sh
netstat -n | awk '/^tcp/ {++State[$NF]} END {for(i in State) print i, State[i]}'
```



Catal.sh中添加启动参数

```hxml
 	JAVA_OPTS="$JAVA_OPTS  -Xms3g -Xmx3g -XX:PermSize=1g -XX:MaxPermSize=1g -Xshare:off -Xmn1024m -XX:+PrintGC -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -XX:+PrintHeapAtGC -XX:MetaspaceSize=152M -XX:MaxMetaspaceSize=152M -XX:+UseConcMarkSweepGC -XX:+UseParNewGC "
```



tomcat启动连接池

```
 <Executor name="tomcatThreadPool" namePrefix="catalina-exec-"
        maxThreads="500" minSpareThreads="4"/>
```



# jvm 调优

jmap -heap pid

查看jvm 内存使用情况

![image-20200103152705696](/Users/haochencheng/Library/Application Support/typora-user-images/image-20200103152705696.png)





查看内存中对象信息

```shell
jmap -histo pid
```

查看存活的对象

```shell
jmap -histo:live pid 
```



查看使用的那种垃圾收集器

```shell
 java -XX:+PrintCommandLineFlags -version
```

结果：

```
-XX:InitialHeapSize=1048988736 -XX:MaxHeapSize=16783819776 -XX:+PrintCommandLineFlags -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseParallelGC 
openjdk version "1.8.0_232"
OpenJDK Runtime Environment (build 1.8.0_232-b09)
OpenJDK 64-Bit Server VM (build 25.232-b09, mixed mode)
```

UseParNewGC和UseParallelGC的区别

```
1、UseParNewGC：并发串行收集器，它是工作在新生代的垃圾收集器，它只是将串行收集器多线程化，除了这个并没有太多创新之处，而且它们共用了相当多的代码。它与串行收集器一样，也是独占式收集器，在收集过程中，应用程序会全部暂停。但它却是许多运行在Server模式下的虚拟机中首选的新生代收集器，其中有一个与性能无关但很重要的原因是，除了Serial收集器外，目前只有它能与CMS收集器配合工作。

2、UseParallelGC：并行收集器，同时运行在多个cpu之间，物理上的并行收集器，跟上面的收集器一样也是独占式的，但是它最大化的提高程序吞吐量，同时缩短程序停顿时间，另外它不能与CMS收集器配合工作。
```

