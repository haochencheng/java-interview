### 模拟java内存溢出
编译打包 
```bash
mvn clean install  -DskipTests=true
```

####    server.sh
服务启动脚本
- 启动程序参数$1  
    start status stop restart
- 启动程序参数$2
    指定内存参数 low mid big
### 模拟tomcat 线程被占满 cpu高负载

1.程序启动 
已low memory参数启动 
LOW_MEMORY="-Xmx52m -Xms52m"

```bash
sh server.sh start low 
```

查看程序线程数
```bash
sh bin/jvm.sh thread
```


执行测试脚本


