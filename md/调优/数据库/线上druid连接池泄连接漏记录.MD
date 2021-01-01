###	线上现象

线上服务器负载高

线程cpu占用率高

客户端请求接口一直在等待 没有响应

java线程 很多在 pending 

很多垃圾回收线程

java 线程 总数量 500多。正常300多。

查看数据库 很多连接 都是 sleep 

###	所用命令

linux top 命令，jdk 工具 jps jstack 等  如果安装openjdk 则需要 安装插件。

mysql数据库 命令

###	查看CUP信息

#####	cpu个数

```sh
cat /proc/cpuinfo | grep 'physical id' | sort | uniq | wc -l
```

#####	cpu核数

```sh
cat /proc/cpuinfo |grep "cores"|uniq|awk '{print $4}'
```

#####	逻辑 CPU 核数

```sh
cat /proc/cpuinfo |grep "processor"|wc -l
```

#####	查看系统负载

```sh
uptime
```

#####	load average 系统平均负载

```txt
load average: 0.14, 0.45, 0.64
它们的意思分别是1分钟、5分钟、15分钟内系统的平均负荷。
```

单核满载是1 ， n核满载是n

###	查看服务器负载

```sh
top
```

#####	找出程序进程

```sh
ps -ef | grep tomcat-app
```

#####	查看进程内线程

```sh
Top -Hp 11087
```

###	找出占用cpu 高的线程 

```sh
printf "%x\n"  线程id
```

###	先看对应线程信息

```
jstack 进程id
```

查看进程中线程状态 发现大多数线程都在pending ，gc很频繁

```sh
jstack 11087 | grep 10f1e 
```



###	数据库相关命令

**查询数据库正在执行事物**

```
SELECT * FROM information_schema.INNODB_TRX
```

**查看正在锁的事务**

```
SELECT * FROM INFORMATION_SCHEMA.INNODB_LOCKS;
```

**查看等待锁的事务**

```
SELECT * FROM INFORMATION_SCHEMA.INNODB_LOCK_WAITS;
```

**查看连接数**

```sql
show processlist;
```

查看连接状态，连接时间，对比连接池配置文件中最大连接数 等参数

最后通过报错日志 发现数据库连接已用尽，打开 druid 监控 发现 有一个 sql 执行完成 但是并没有 释放sql连接

解决方法 ： 回退druid 版本 。之后压测发现没有问题 



```
 trace com.urwork.app.controller.ReformerController getOfflinePermissions -n 10   
```



```
 trace com.urwork.app.controller.CardController appOpenDoor -n 10   
```

