https://www.jianshu.com/p/0d0f3ee83bf1

## hash分区规则

**节点取余分区规则、一致性哈希分区（Consistent hashing）、虚拟槽（Virtual slot）分区。**



## 节点取余分区

 使用特定的数据，如Redis的键或用户ID，再根据节点（运行在集群模式下的Redis服务器）的数量N使用公式：hash(key) % N计算出hash值，用来决定数据存储在哪个节点上。例如，将20个数据存储在4个节点上：

![节点取余](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/hash分区/节点取余分区1.png)

​													4个节点存储20个数据

如果此时增加一个节点，那么经过重新hash计算后得到的分布如下（其中数据1、2、3、20这4个数据存储的位置没有改变，其他的数据位置都发生了改变。）：





​													5个节点存储20个数据

