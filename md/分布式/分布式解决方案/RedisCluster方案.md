https://www.jianshu.com/p/42e2a06e0d09

https://zhuanlan.zhihu.com/p/69800024 

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

###  CLUSTER ADDSLOTS 命令的实现

```
(1) 遍历所有输入的槽，检查它们是否都未被指派。
(2) 如果存在任何一个槽已经被指派，那么向客户端返回错误，并终止命令。
(3) 如果这些输入槽都没有被指派，再次遍历所有输入槽，设置clusterState结构的slots数组，将slots[i]的指针指向代表当前节点的clusterNode结构。接着访问当前节点的clusterNode结构的slots数组，将数组在索引i的二进制设置为1.
```

下图表示给槽1、2指派节点的过程：

![clusterState](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/槽指派.png)

## 在集群中执行命令

对数据库中的16384个槽指派后，集群就处于上线状态，这时客户端就可以向集群中的节点发送数据命令了。
  当客户端向节点发送与数据库键有关的命令时，接收命令的节点会计算出要处理的数据库键属于哪个槽，并检查这个槽是否分配给了自己：

```
如果键所在的槽正好就是指派了个当前节点，那么节点直接执行这个命令。
如果键所在槽并没有指派给当前节点，那么节点会向客户端返回一个MOVED错误，指引客户端转向（redirect）至正确的节点，并再次发送之前想要执行的命令。

```

![客户端执行命令流程](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/客户端执行命令流程.png)

###  计算键属于哪个槽

节Redis首先需要计算键所对应的槽。根据槽的有效部分使用CRC16函数计算出散列值，再取对16383的余数，每个键都可以映射到0~16383槽范围内。
  如果键内容包含“{” 和“}”大括号字符，大括号内的内容又叫做hash_tag，则计算槽的有效部分是括号内的内容，这就会使得即使键不相同，也可以使键具有相同的slot的功能，常用于Redis IO优化。例如在集群模式下使用mget等命令优化批量调用时，键列表必须具有相同的slot，否则会报错。这时可以利用hash_tag让不同的键具有相同的slot达到优化的目的。

 使用*CLUSTERKYESLOT *命令可以查看一个键属于哪个槽，如：

```
127.0.0.1:7000> cluster keyslot "book";
(integer) 1337
127.0.0.1:7000> cluster keyslot "date";
(integer) 2202
127.0.0.1:7000> cluster keyslot "lst";
(integer) 3347
127.0.0.1:7000> cluster keyslot "name";
(integer) 5798
127.0.0.1:7000> cluster keyslot "msg";
(integer) 6257

```

使用hash_tag让不同的键具有相同的槽：

```
127.0.0.1:7000> cluster keyslot "book{fruits}haha";
(integer) 14943
127.0.0.1:7000> cluster keyslot "date{fruits}hehe";
(integer) 14943
127.0.0.1:7000> cluster keyslot "lst{fruits}xixi";
(integer) 14943
```

### 判断槽是否由当前节点负责处理

 计算出键所属的槽后，节点就会检查自己在clusterState.slots数组中的项i，判断键是否属于负责。
例如，客户端向节点7000发送命令：

```
127.0.0.1:7000>set msg"happy new year!";
```

节点首先计算出键msg属于的槽6257，然后检查clusterState.slots[6257]是否等于clusterState.myself，结果发现并不相等，说明该槽不是节点7000负责处理，于是节点7000访问clusterState.slots[6257]所指向的clusterNode结构，并根据结构中的IP地址127.0.0.1和端口号7001，向客户端返回错误MOVED 6257 127.0.0.1:7001，指引节点转向至正在负责处理槽6257的节点7001。

![set命令通过槽获取处理节点](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/set命令通过槽获取处理节点.png)

### MOVED错误

 MOVED错误的格式为：*MOVED* <slot> <ip>:<port>

 当客户端接收到节点返回的*MOVED*错误时，客户端会根据*MOVED*的错误中提供的IP地址和端口号，转向负责处理槽slot的节点，并向该节点重新发送之前想要执行的命令。

![客户端MOVED错误](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/客户端MOVED错误.png)

### 节点数据库的实现

集群节点和单机服务器在保存数据方式上完全相同，唯一的区别就是节点只能使用0号数据库，而单机Redis服务器则没有这一限制。

```
  Redis服务器默认会创建16个数据库，默认情况下，Redis客户端的目标数据库为0号数据库，客户端可以通过执行SELECT命令来切换目标数据库。
```

 此外，除了将键值对保存在数据库里，节点还会用clusterState结构中的slots_to_keys[跳跃表](https://www.jianshu.com/p/0ceb34c2116a)保存槽和键之间的关系。
  slots_to_keys跳跃表的每个节点的成员（member）都是一个数据库键，而每个节点的分值（score）都是该键所属的槽号。

```
每当节点往数据库中添加一个新的键值对时，节点就会将这个键以及键的槽号关联到slots_to_keys跳跃表
当节点删除数据库中的某个键值对时，节点就会在slots_to_keys跳跃表解除被删除键与槽号的关联。
```

 例如，对于数据库键book（slot = 1337）、date（slot = 2022）和lst（slot = 3347），节点7000将会创建如下的slots_to_keys跳跃表：

![键槽跳跃表](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/键槽跳跃表.png)



通过跳跃表中记录各个数据库键所属的槽，节点可以很方便地对属于某个或某些槽的所有的数据库键进行批量操作。例如，要想删除槽1337至3347的所有数据库键，通过跳跃表查询到分值为1337和3347的节点，然后使用链表的删除操作直接将两个节点之间的节点删除即可。命令*CLUSTER GETKEYSINSLOT  *命令返回最多count的数据库键，而这个命令就是通过slots_to_keys跳跃表实现的。

## 重新分片

Redis的重新分片是指可以将任意数量已经指派给某个节点（源节点）的槽指派给另一个节点（目标节点），并且相关槽的键值对也会从源节点被移动到目标节点。
  重新分片可以在线（online）进行，在重新分片的过程中，集群不用下线，并且源节点可以继续处理命令请求。

 例如，对于之前提到的，包含 7000 、7001 和7002 三个节点的集群来说，可以向这个集群添加一个 IP 为 127.0.0.1，端口号为 7003 的节点（后面简称节点 7003)。然后通过重新分片操作， 将原本指派给节点 7002 的槽 15001 至 16383 改为指派给节点 7003。

![集群添加节点](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/集群添加节点.png)

### 重新分片的实现原理

 对单个槽slot进行重新分片的步骤：

```
(1) 源节点和目标节点之间的通信，源节点通知目标节点准备迁移槽。
(2) 通过命令CLUSTER GETKEYSINSLOT <slot> <count>从槽slot中最多获取count个数据库键名，并将每个键通过MIGRATE 命令发送给目标节点迁移至目标节点。并重复此步骤直到槽slot中所有的键值对都被迁移到了目标节点。
(3) 槽数据迁移完成后，会向集群中的任意一个节点发送一条信息，表示槽slot已经被指派给目标节点了，这一指派信息会通过消息发送给整个集群，最终集群中的所有节点都会知道这一指派信息。
```

![重新分片流程](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/重新分片流程.png)

如果重新分片涉及多个槽，那么将对每个槽都执行上面的操作。

## ASK错误

 在进行重新分片期间，源节点向目标节点迁移一个槽的过程中，可能会出现这样一种情况：属于被迁移槽的一部分键值对保存在源节点里面，而另一部分键值对则保存在目标节点中。
  当客户端向源节点发送一个与数据库键有关的命令，并且命令要处理的数据库键器恰好就属于正在被迁移的槽时：

```
源节点会先在自己的数据库里面查找指定的键，如果找到，就直接执行客户端发送的命令。
如果源节点没能在自己的数据库里面找到指定的键，那么这个键有可能已经被迁移到了目标节点，源节点向客户端返回一个ASK错误，指引客户端向正在导入槽的目标节点，并再次发送之前想要执行的命令。
```

**ASK错误和MOVED错误的区别：**

MOVED错误代表槽的负责权已经从一个节点转移到另一个节点：在客户端收到关于槽i的MOVED错误之后，客户端每次遇到关于槽i的命令请求时，都可以直接将命令请求发送至MOVED错误所指向的节点，因为该节点就是目前负责槽i节点。

![ASK流程](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/MOVED流程.png)

ASK错误只是两个节点在迁移槽的过程中使用的一种临时措施：在客户端收到关于槽i的ASK错误之后、客户端只会在接下来的一次命令请求中将关于槽i的命令请求发送至ASK错误所指示的节点，但是这种转向不会对客户端今后发送关于槽i的命令请求产生任何影响，客户端仍然会将关于槽i的命令请求发送至目前负责处理槽i的节点，除非ASK错误再次出现。

![ASK流程](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/ASK流程.png)

## 复制与故障转移

Redis集群中的节点分为主节点（master）和从节点（slave），其中主节点用于处理槽，而从节点则用于复制某个主节点，并在复制的主节点下线时，代替下线主节点继续处理命令。
  例如，对于包含7000、7001、7002和7003四个主节点的集群里，现在将7004和7005两个节点添加到集群里，并将这两个节点设定为7000的从节点。

![集群设置从节点](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/集群设置从节点.png)

 当节点7000进入下线状态，那么集群中仍在正常运作的几个主节点将在节点7000的两个从节点——节点7004和节点7005中选出一个节点作为新的主节点，如7004作为主节点，并继续处理客户端发送的命令。

![集群切换主从](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/集群切换主从.png)

 当下线的主节点7000重新上线，那么它将成为节点7004的从节点。

![集群节点恢复](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/集群切换主从.png)

### 设置从节点

向一个节点发送命令：*CLUSTER REPLICATE* <node_id>,可以让接收命令的节点成为node_id所指定的从节点。

 一个节点成为从节点，并开始复制某个主节点这一个信息会通过消息发送给集群中的其他节点，最终集群中所有节点都会知道某个节点正在复制某个主节点。

### 故障检测

  集群中的每个节点都会定期地向集群中的其他节点发送*PING*消息，以此来检测对方是否在线，如果接收*PING*消息的节点没有在规定的时间内，向发送*PING*消息的节点返回*PONG*消息，那么发送*PING*消息的节点就会接收*PING*消息的节点标记为**疑似下线（probable fail, PFAIL）**
  集群中各个节点会通过互相发送消息的方式来交换集群中各个节点的状态信息，如某个节点是处于在线状态、疑似下线状态（PFAIL），还是已下线状态（FAIL）。

如果在一个集群里面，半数以上负责处理槽的主节点都将某个主节点A报告为疑似下线，那么这个主节点A将被标记为**已下线（FAIL）**，并且将主节点A标记为已下线的节点会向集群广播一条关于主节点A的FAIL消息，所有收到这条FAIL消息的节点都会立即将主节点A标记为已下线。
  例如，主节点7002和7003已经认为主节点7000进入了疑似下线状态，并且主节点7001也认为主节点7000进入下线状态，此时，集群中已经有超过一半数量的主节点将主节点7000标记为下线，所以主节点7001会将主节点7000标记为已下线，并向集群广播一条关于主节点7000的FAIL消息。

![集群广播FAIL消息](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/集群广播FAIL消息.png)

