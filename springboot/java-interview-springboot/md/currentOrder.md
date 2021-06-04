# 并发下订单
根据sql文件创建测试数据库和表    
![数据库sql](../sql/order.sql) 

编译打包 
```bash
mvn clean install  -DskipTests=true
```

启动程序
```bash
sh bin/server start big
```

执行下单测试脚本
```bash
sh bin/test.sh order
```

### 单机并发
mysql数据库，更新库存时候用数据库cas  
```
do{
     v=原值
}while(!update(v-1，v))
```

4核8g java启动参数 2g jdk1.8 
tomcat8 线程池 200 请求队列 900
1000 qps 创建订单 平均响应 300ms 
2000 qps 创建订单 平均响应 500ms 

### 分布式高并发
考虑的几个点
####   1.流水号
并发场景下，分布式 请求流水号 高效不重复。  
eg. 
#####   1.1雪花算法  
缺点：太长了
优点：高效，不重复
#####   1.2 机器标识+年月日时分秒毫秒+随机数2位
serialNo=01-20200718192030123-10
#####   1.3 一台机器专门生成流水号
优点：不会重复，统一管理
缺点：多一次请求调用，机器需要集群，容易单点。
#####   1.4 根据用户id 生成流水号
优点：高效，唯一，不会重复

####    2.限流

####    3.分布式锁
保持数据一致性
#####   3.1 redis分布式锁
setnx + 重试 +超时
#####   3.2 zookeeper分布式锁

#####   3.2 三方raft提供分布式锁


####    4.高可用
并发服务资源隔离，不影响其他业务
#####   4.1 服务器隔离
k8s，弹性伸缩，动态扩容
#####   4.2 服务隔离
服务单独部署，和其他服务在一个应用需要做线程隔离
#####   4.3 数据库隔离
分库



   


 
