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

```
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

