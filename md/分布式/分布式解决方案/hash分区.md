https://www.jianshu.com/p/0d0f3ee83bf1

## hash分区规则

**节点取余分区规则、一致性哈希分区（Consistent hashing）、虚拟槽（Virtual slot）分区。**



## 节点取余分区

 使用特定的数据，如Redis的键或用户ID，再根据节点（运行在集群模式下的Redis服务器）的数量N使用公式：hash(key) % N计算出hash值，用来决定数据存储在哪个节点上。例如，将20个数据存储在4个节点上：



