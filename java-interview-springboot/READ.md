### springboot
编译打包 
```bash
mvn clean install 
```

####    server.sh
服务启动脚本
- 启动程序参数$1  
    start status stop restart
- 启动程序参数$2
    指定内存参数 low mid big
### 模拟java OutOfMemoryError
```bash
java.lang.OutOfMemoryError: Java heap space
```

1.程序启动 
已low memory参数启动 
LOW_MEMORY="-Xmx52m -Xms52m"

```bash
sh server.sh start low 
```

查看程序对象内存
```bash
sh bin/jvm.sh histo
```

查看程序heap信息
```bash
sh bin/jvm.sh heap
```

执行测试脚本
模拟添加大对象
请求地址，每次添加1m的byte[]对象
```bash
pers.interview.springboot.controller.addBigObject
```
查看server/error.log中 有内存溢出的错误日志
```text
java.lang.OutOfMemoryError: Java heap space
	at pers.interview.springboot.controller.BigObjectController.addBigObject(BigObjectController.java:26)
	at jdk.internal.reflect.GeneratedMethodAccessor21.invoke(Unknown Source)
```

```bash
 sh bin/test.sh outOfMemory 
```

再次生成堆信息可以看到内存已用尽
```bash
sh bin/jvm.sh heap
```
查看程序对象内存 第一的大对象就是我们刚才添加的对象
```bash
sh bin/jvm.sh histo

```

```text
num 	  #instances	#bytes	Class description
--------------------------------------------------------------------------
1:		36257	22330216	byte[]
```

