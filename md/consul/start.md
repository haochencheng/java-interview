### 快速开始 
mac安装consul
```
brew install consul
```

1. 启动consul agent在 开发模式
```
consul agent -dev
```

2. 查看数据中心members
```
consul members
```

3. http
```
curl localhost:8500/v1/catalog/nodes
```

4. 停止agent
···
consul leave
···

### 服务注册
Register a Service with Consul Service Discovery
