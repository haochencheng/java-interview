### IO模型
参考资料：   https://segmentfault.com/a/1190000003063859

#####   概念说明
在进行解释之前，首先要说明几个概念：
- 用户空间和内核空间
- 进程切换
- 进程的阻塞
- 文件描述符
- 缓存 I/O


####     IO模式



#####   IO多路复用
![ioMultiplexing](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/io/iomultiplexing.png)




#####   select
```C
    int select (int n, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, struct timeval *timeout);
``` 
select 函数监视的文件描述符分3类，分别是writefds、readfds、和exceptfds。
调用后select函数会阻塞，直到有描述副就绪（有数据 可读、可写、或者有except），或者超时（timeout指定等待时间，如果立即返回设为null即可），函数返回。
当select函数返回后，可以 通过遍历fdset，来找到就绪的描述符。