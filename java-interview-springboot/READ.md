### springboot
编译打包 
```bash
mvn clean install 
```
程序启动 
```bash
sh server.sh start low 
```
$1 start status stop restart
$2 low mid big

查看程序参数  
jdk8  
```bash
 jmap -heap 37553
```
jdk11   
```bash
 jhsdb jmap  --heap --pid  37553
```
