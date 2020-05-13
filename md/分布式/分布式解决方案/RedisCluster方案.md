 Redis Cluster是Redis分布式解决方案，集群通过分片(sharing)来对数据共享，并提供复制和故障转移功能。
  本文将介绍集群的**节点、槽指派、命令执行、重新分片、转向、故障转移、消息。**

一个Redis集群通常由多个节点（node，就是运行在集群模式式下的Redis服务器）组成，刚开始的时候，每个节点都是相互独立的，它们处于一个只包含自己的集群中，要组建一个集群，就必须将多个独立的节点连接起来，构成一个包含多个节点的集群。
  连接各个节点的工作可以使用*CLUSTER MEET*命令来完成，格式为：

```
CLUSTER MEET <ip> <port>
```

向一个节点node发送CLUSTER MEET命令，可以让node节点的ip和port所指定的节点进行握手（handshake），当握手成功时，node节点就会将ip和port所指定的节点添加到node节点当前所在的集群里。

 例如下图，假设有三个独立运行的节点127.0.0.1:7000、127.0.0.1:7001、127.0.0.1:7002（下文省略ip地址，直接使用端口号来区分各个节点），它们各自处于自己的集群里。

![manyNode](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/redisCluster/manyNode.png)



首先使用客户端连接上节点7000，通过向节点7000发送命令：

```
127.0.0.1:7000> CLUSTER MEET 127.0.0.1 7001
OK
```

 执行成功就表示节点7001成功加入到节点7000所在的集群，可以通过命令*CLUSTER NODES*查看集群中的节点。

![nodeJoinCluster](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/redisCluster/nodeJoinCluster.png)

 同理执行命令：

```
127.0.0.1:7000> CLUSTER MEET 127.0.0.1 7003
OK
```

![NodeJoinCluster1](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/redisCluster/NodeJoinCluster1.png)

 以上就是几个相互独立的节点通过握手组建成了一个集群。下面介绍组建成一个集群过程中的过程及*CLUSTER MEET*命令实现的原理。

### 启动节点

 一个节点就是运行在集群模式下的Redis服务器，Redis服务器在启动时会根据cluster_enable配置选项是否为yes来决定是否开启服务器集群模式，如下图所示：

![clusterStart](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/redisCluster/clusterStart.png)



**节点会使用所有在单机模式中使用的服务器组件**，如使用数据库保存键值对、使用*RDB*持久化模块和*AOF*持久化模块、使用发布与订阅模块来执行*PUBLISH*和*SUBSCRIBE*等命令。除此之外，在集群模式下使用到的数据，节点将他们保存在clusterNode结构、clusterLink结构和clusterState结构中。

### 集群数据结构

clusterNode结构保存了一个节点当前的状态，如节点的创建时间、节点的名字、节点的IP地址和端口号等。每个节点都会创建一个clusterNode结构来记录自己的状态，并为集群里所有其他节点（包括主节点和从节点）都创建相应的clusterNode结构，以记录其他节点的状态。

```cpp
struct clusterNode{
    // ...
    // 创建节点的时间
    mstime_t time;
    // 节点的名字，由40个十六进制字符组成
    char name[REDIS_CLUSTER_NAMELEN]
    // 节点标识，用来标识节点的角色（如主节点或从节点）、节点目前状态，（如在在线或下线）
    int flag;
    // 保存连接节点所需的有关信息
    clusterLink *link
    // 节点的端口
    int port;
    // ...
}
```

 clusterNode结构的link属性是一个clusterLink结构，该结构保存了连接节点所需的有关信息，如套接字描述符，输入缓冲区和输出缓冲区。

```cpp
typedef struct clusterLink{
    // ...
    // 与这个连接相关联的节点，如果没有的化就为NULL
    struct clusterNode * node;
    // TCP套接字描述符
    int fd;
    // 输出缓冲区，保存着等待发送给其他节点的消息
    sds sndbuf;
    // 输入缓冲区，保存着从其他节点接收到的消息
    sds rcvbuf;
    //...
} clusterLink
```

最后，每个节点都保存着一个clusterState结构，这个结构记录了在当前节点的视角下，集群目前的状态，如集群是在线还是下线，集群包含多少个节点，集群当前的配置纪元等

```cpp
typedef struct clusterState{
    // ...
    // 指向当前节点的指针
    sclusterNode * myself
    // 集群当前的配置纪元，用于故障转移
    unit64_t currentEpoch;
    // 集群当前的状态
    int state;
    // 集群中节点的数量
    int size;
    // 集群中节点（包括myself节点）
    dict *nodes;
    //...
} clusterState
```

 以前面的7000、7001和7002三个节点为例，节点7000创建的clusterState结构如下图，同理，节点7001和节点7002也会创建类似的clusterState结构。

![threeNode](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/threeNode.png)

### *CLUSTER MEET*命令的实现

通过向节点A发送*CLUSTERMEET*命令，客户端让接受命令的节点A将另一节点添加到当前的所在的集群中。

```
(1) 节点A和节点B会首先进行握手，以此确认彼此的存在，然后节点A会为节点B创建一个clusterNode结构，添加到自己的clusterState.nodes字典里。
 (2) 之后，节点A会根据给定的IP地址和端口号发送一条*MEET*消息，节点B接收到消息后也会创建一个clusterNode结构添加到自己的clusterState.nodes字典里。
 (3) 最后，两个节点通过发送*PING*命令和返回*PONG*命令来确保各自成功的响应了对方的命令。
```



![握手过程](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/握手过程.png)

 握手成功后，节点A会将节点B的信息通过Gossip协议传播给集群中的其他节点，让其他节点与节点B握手，最终，节点B会被集群中的所有节点认识。

##  槽指派

 **Redis集群通过分片的方式来保存数据库中的键值对：集群的整个数据库被分为16384个槽（slot），数据库中的每个键都属于这个16384个槽中的一个，集群中的每个节点可以处理0个或最多16384个槽。
  当数据库中的16384个槽都有节点在处理时，集群处于上线状态（ok）；相反的，如果数据库中有任何一个槽没有得到处理，那么集群处于下线状态（fail）。**

### 记录节点的槽指派信息

**每个节点负责处理哪些槽都被记录在各自的clusterNode结构的slots属性和numslots属性中。**

```
typedef struct clusterNode{
    // ...
    // 存储节点负责处理的槽位
    clusterNode *slots[16384];
   //  节点负责处理槽的数量 
    int numslot;
    // ...
}
```



 slots是一个二进制位数组（bit array），这个数组长度为16384/8 = 2048个字节，共包含16384个二进制位。numslots表示该节点负责处理多少个槽。如果节点A负责处理槽i，那么就在A节点的clusterNode结构中的slots数组中将索引i的二进制位设为1。同理，如果节点B负责处理槽j，那么就在B节点的clusterNode结构中的slots数组中将索引i的二进制位设为1。
  可以执行*CLUSTER ADDSLOTS*命令给节点分配槽，例如：

```
127.0.0.1:7000 > CLUSTER ADDSLOTS 0 1 2 ... 5000
OK
```

```
127.0.0.1:7001 > CLUSTER ADDSLOTS 5001 5002 ... 10000
OK
```

```
127.0.0.1:7002 > CLUSTER ADDSLOTS 10001 10002 ... 16383
OK
```

通过以上三个命令就分别将槽0-5000指派给节点7000，槽5001-10000指派给节点7001，槽10001-18383指派给节点7002。当所有的槽被指派完后，集群就处于上线状态。

### 传播节点的槽指派信息

一个节点除了会将自己负责的槽记录在clusterNode结构的slots属性和numslots属性之外，它还会将自己的slots数组通过发送给集群中的其他节点，以此来告知其他节点自己目前负责处理哪些槽。例如，对节点7000、7001和7002来说，它们会告知彼此自己负责的槽。

![](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/槽处理.png)

![](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/槽处理1.png)

### 记录集群所有槽的指派信息

 clusterState结构中也有一个slots数组，它记录的是集群中所有16384个槽的指派信息。例如，对于7000、7001和7002三个节点，它们的clusterState结构中的slots数组都一样，如下图：

![clusterState](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/clusterState.png)

```
  (1) 前面说了每个节点都会知道数据库中的16384个槽分别指派给了集群中哪些节点，为什么还要在clusterState结构中使用slots数组呢？
    这是因为虽然节点知道槽指派给了哪些节点，但是如果要想知道槽i是否已经被指派或者指派给了哪个节点，只能通过遍历clusterNode.nodes字典中的所有的clusterNode结构，检查这些结构的slots数组，知道找到为止，这个过程的复杂度为O(N)。相反，如果在clusterState结构中保存了槽指派信息的slots数组，只需检查该数组中的槽i的指派情况即可，这个过程时间复杂度为O(1)。
  (2) 两个结构中的slots数组的区别：clusterState.slots数组记录了集群中所有槽的指派信息，而clusterNode.slots数组只记录了clusterNode结构所代表的的节点的槽的指派信息。

```

