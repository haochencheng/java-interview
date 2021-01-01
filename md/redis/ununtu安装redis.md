# 编译安装

https://redis.io/download

redis 官网下载redis



wget http://download.redis.io/releases/redis-6.0.1.tar.gz

解压

```
tar -zxvf redis-6.0.1.tar.gz
```

编译

```
sudo make && sudo make instlal 
```

如果出现

```
zmalloc.h:50:31: 致命错误：jemalloc/jemalloc.h：没有那个文件或目录
```

则使用 如下命令编译

```
sudo make MALLOC=libc && sudo make instlal 
```

会安装到目录/usr/local/bin下：

进入到/usr/local/bin目录下，执行命令：（默认端口6379）

```linux
redis-server /etc/redis/redis.conf
redis-cli -p 6379
```

然后执行命令ping，若输出为pong，则证明服务成功启动。

