# JVM内存管理

​    根据JVM规范，JVM把内存划分成了如下几个区域：

```
1.方法区（Method Area）
2.堆区（Heap）
3.虚拟机栈（VM Stack）
4.本地方法栈（Native Method Stack）
5.程序计数器（Program Counter Register）
```

## 2.1 方法区（Method Area）

​    方法区存放了要加载的类的信息（如类名、修饰符等）、静态变量、构造函数、final定义的常量、类中的字段和方法等信息。方法区是全局共享的，在一定条件下也会被GC。当方法区超过它允许的大小时，就会抛出OutOfMemory：PermGen Space异常。

​    在(jdk1.7)Hotspot虚拟机中，这块区域对应**持久代（Permanent Generation）**，一般来说，方法区上执行GC的情况很少，因此方法区被称为持久代的原因之一，但这并不代表方法区上完全没有GC，其上的GC主要针对常量池的回收和已加载类的卸载。在方法区上进行GC，条件相当苛刻而且困难。

​    **运行时常量池（Runtime Constant Pool）**是方法区的一部分，用于存储编译器生成的常量和引用。一般来说，常量的分配在编译时就能确定，但也不全是，也可以存储在运行时期产生的常量。比如String类的intern（）方法，作用是String类维护了一个常量池，如果调用的字符"hello"已经在常量池中，则直接返回常量池中的地址，否则新建一个常量加入池中，并返回地址。

Jdk8 改为元数据区。不会在有PermGen Space异常。

## 2.2 堆区（Heap）

​    堆区是GC最频繁的，也是理解GC机制最重要的区域。堆区由所有线程共享，在虚拟机启动时创建。堆区主要用于存放对象实例及数组，所有new出来的对象都存储在该区域。

## 2.3 虚拟机栈（VM Stack）

​    虚拟机栈占用的是操作系统内存，每个线程对应一个虚拟机栈，它是线程私有的，生命周期和线程一样，每个方法被执行时产生一个**栈帧（Statck Frame）**，栈帧用于存储局部变量表、动态链接、操作数和方法出口等信息，当方法被调用时，栈帧入栈，当方法调用结束时，栈帧出栈。

​    **局部变量表**中存储着方法相关的局部变量，包括各种基本数据类型及对象的引用地址等，因此他有个特点：内存空间可以在编译期间就确定，运行时不再改变。

​    虚拟机栈定义了两种**异常类型**：**StackOverFlowError(栈溢出)和OutOfMemoryError（内存溢出）**。如果线程调用的栈深度大于虚拟机允许的最大深度，则抛出StackOverFlowError；不过大多数虚拟机都允许动态扩展虚拟机栈的大小，所以线程可以一直申请栈，直到内存不足时，抛出OutOfMemoryError。

## 2.4 本地方法栈（Native Method Stack）

​    **本地方法栈**用于支持native方法的执行，存储了每个native方法的执行状态。本地方法栈和虚拟机栈他们的运行机制一致，唯一的区别是，虚拟机栈执行Java方法，本地方法栈执行native方法。在很多虚拟机中（如Sun的JDK默认的HotSpot虚拟机），会将虚拟机栈和本地方法栈一起使用。

## 2.5 程序计数器（Program Counter Register）

​    **程序计数器**是一个很小的内存区域，不在RAM上，而是直接划分在CPU上，程序猿无法操作它，它的作用是：JVM在解释字节码（.class）文件时，存储当前线程执行的字节码行号，只是一种概念模型，各种JVM所采用的方式不一样。字节码解释器工作时，就是通过改变程序计数器的值来取下一条要执行的指令，分支、循环、跳转等基础功能都是依赖此技术区完成的。

​    每个程序计数器只能记录一个线程的行号，因此它是线程私有的。

​    如果程序当前正在执行的是一个java方法，则程序计数器记录的是正在执行的虚拟机字节码指令地址，如果执行的是native方法，则计数器的值为空，此内存区是唯一不会抛出OutOfMemoryError的区域。

## 3.1 查找算法

​    经典的**引用计数算法**，每个对象添加到引用计数器，每被引用一次，计数器+1，失去引用，计数器-1，当计数器在一段时间内为0时，即认为该对象可以被回收了。但是这个算法有个明显的缺陷：当两个对象相互引用，但是二者都已经没有作用时，理应把它们都回收，但是由于它们相互引用，不符合垃圾回收的条件，所以就导致无法处理掉这一块内存区域。因此，Sun的JVM并没有采用这种算法，而是采用一个叫——**根搜索算法**

基本思想是：从一个叫GC Roots的根节点出发，向下搜索，如果一个对象不能达到GC Roots的时候，说明该对象不再被引用，可以被回收

 补充概念，在JDK1.2之后引入了四个概念：**强引用、软引用、弱引用、虚引用**。
    **强引用**：new出来的对象都是强引用，GC无论如何都不会回收，即使抛出OOM异常。
    **软引用**：只有当JVM内存不足时才会被回收。
    **弱引用**：只要GC,就会立马回收，不管内存是否充足。
    **虚引用**：可以忽略不计，JVM完全不会在乎虚引用，你可以理解为它是来凑数的，凑够"四大天王"。它唯一的作用就是做一些跟踪记录，辅助finalize函数的使用。

## 3.2 内存分区

​    内存主要被分为三块：**新生代（Youn Generation）、旧生代（Old Generation）、持久代（Permanent Generation）**。三代的特点不同，造就了他们使用的GC算法不同，新生代适合生命周期较短，快速创建和销毁的对象，旧生代适合生命周期较长的对象，持久代在Sun Hotpot虚拟机中就是指方法区（有些JVM根本就没有持久代这一说法）。

 **新生代（Youn Generation）**：分为Eden区和Survivor区，Survivor区又分为大小相同的两部分：FromSpace和ToSpace。新建的对象都是从新生代分配内存，Eden区不足的时候，会把存活的对象转移到Survivor区。当新生代进行垃圾回收时会出发**Minor GC**（也称作**Youn GC**）。

​    **旧生代（Old Generation）**：旧生代用于存放新生代多次回收依然存活的对象，如缓存对象。当旧生代满了的时候就需要对旧生代进行回收，旧生代的垃圾回收称作Major GC（也称作Full GC）。

![](https://static001.infoq.cn/resource/image/d4/34/d4e6583cd0ecfa60622526e7a4f13634.png)

## 3.3 GC算法

​    **常见的GC算法**：**复制、标记-清除和标记-压缩**

​    **复制**：复制算法采用的方式为从根集合进行扫描，将存活的对象移动到一块空闲的区域，如图所示：

当存活的对象较少时，复制算法会比较高效（新生代的Eden区就是采用这种算法），其带来的成本是需要一块额外的空闲空间和对象的移动。

  **标记-清除**：该算法采用的方式是从跟集合开始扫描，对存活的对象进行标记，标记完毕后，再扫描整个空间中未被标记的对象，并进行清除。

​			XXOXOOXO

​			XX_X__X_

标记-清除动作不需要移动对象，且仅对不存活的对象进行清理，在空间中存活对象较多的时候，效率较高，但由于只是清除，没有重新整理，因此会造成内存碎片。

 **标记-压缩**：该算法与标记-清除算法类似，都是先对存活的对象进行标记，但是在清除后会把活的对象向左端空闲空间移动。

​			XXOXOOXO

​			XXXX

由于进行了移动规整动作，该算法避免了标记-清除的碎片问题，但由于需要进行移动，因此成本也增加了。（该算法适用于旧生代）

# 四、垃圾收集器

## 4.1 串行收集器（Serial GC）

 **Serial GC**是最古老也是最基本的收集器，但是现在依然广泛使用，JAVA SE5和JAVA SE6中客户端虚拟机采用的默认配置。比较适合于只有一个处理器的系统。在串行处理器中minor和major GC过程都是用一个线程进行回收的。它的最大特点是在进行垃圾回收时，需要对所有正在执行的线程暂停（stop the world），

## 4.2 ParNew GC

​    基本和Serial GC一样，但本质区别是加入了多线程机制，提高了效率，这样它就可以被用于服务端上（server），同时它可以与CMS GC配合，所以，更加有理由将他用于server端。

## 4.3 Parallel Scavenge GC

​    在整个扫描和复制过程采用多线程的方式进行，适用于多CPU、对暂停时间要求较短的应用，是server级别的默认GC方式。

## 4.4 CMS (Concurrent Mark Sweep)收集器

​    该收集器的目标是解决Serial  GC停顿的问题，以达到最短回收时间。常见的B/S架构的应用就适合这种收集器，因为其高并发、高响应的特点，CMS是基于标记-清楚算法实现的。

CMS收集器的优点：并发收集、低停顿，但远没有达到完美；

CMS收集器的缺点：

```
a.CMS收集器对CPU资源非常敏感，在并发阶段虽然不会导致用户停顿，但是会占用CPU资源而导致应用程序变慢，总吞吐量下降。
b.CMS收集器无法处理浮动垃圾，可能出现“Concurrnet Mode Failure”，失败而导致另一次的Full GC。
c.CMS收集器是基于标记-清除算法的实现，因此也会产生碎片。
```

## 4.5 G1收集器

​    相比CMS收集器有不少改进，首先，基于标记-压缩算法，不会产生内存碎片，其次可以比较精确的控制停顿。

