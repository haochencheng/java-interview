https://cloud.tencent.com/developer/article/1083131

# Eureka Server之间的注册表信息同步

## 前言

Eureka 作为一个服务注册中心，Eureka Server必然是可以通过集群的方式进行部署，但是分布式系统中一个很关键的点就是数据的一致性，多节点部署的Eureka Server必然涉及到不同节点之间的注册表信息的一致性，在CAP中，Eureka 注重的满足了AP，对C只满足的弱一致性(最终一致性)，牺牲了强一致性保证了高可用性，但是Eureka Sever中依然有方式保证节点之间的注册表的信息的一致性。

## **注册表类结构**

首先我们来看一下类图：

![img](https://ask.qcloudimg.com/http-save/yehe-1446357/4dagnbbb4f.jpeg?imageView2/2/w/1620)

在这里`InstanceRegistry`就是Eureka Server注册表的最顶级接口，在内存中维护着注册到Eureka Server中的服务实例的信息

`LeaseManager`定义了对服务实例租约的管理接口

```javascript
public interface LeaseManager<T> {


    void register(T r, int leaseDuration, boolean isReplication);

    boolean cancel(String appName, String id, boolean isReplication);

    boolean renew(String appName, String id, boolean isReplication);

    void evict();
}
```

register(注册)、cancel(下线)、renew(更新)、evict(剔除)，这四个方法对应了Eureka Client与Eureka Server的交互行为相对应，是对注册表信息中的服务实例的租约管理方法，而`Lease`描述了一个基于时限可用的泛型，表示的是一个Eureka服务实例的租约，这里面也提供了关于对其内持有的类的时间有效性的相关操作，它持有的类恰好服务实例的信息`com.netflix.appinfo.InstanceInfo`，下面是该类的关键对象引用和方法：

```javascript
public class Lease<T> {

    // 操作类型
    enum Action {
        Register, Cancel, Renew
    }; 

    public static final int DEFAULT_DURATION_IN_SECS = 90;

    private T holder; //服务实例数据
    private long evictionTimestamp;//服务剔除时间
    private long registrationTimestamp;//注册时间
    private long serviceUpTimestamp;//服务上线时间
    // Make it volatile so that the expiration task would see this quicker
    private volatile long lastUpdateTimestamp;//上次更新时间
    private long duration;//信息有效时长

    public Lease(T r, int durationInSecs) {
        holder = r;
        registrationTimestamp = System.currentTimeMillis();
        lastUpdateTimestamp = registrationTimestamp;
        duration = (durationInSecs * 1000);

    }

    // 服务续约
    public void renew() {
        lastUpdateTimestamp = System.currentTimeMillis() + duration;

    }
    // 服务下线
    public void cancel() {
        if (evictionTimestamp <= 0) {
            evictionTimestamp = System.currentTimeMillis();
        }
    }

    public void serviceUp() {
        if (serviceUpTimestamp == 0) {
            serviceUpTimestamp = System.currentTimeMillis();
        }
    }
   ....
    // 租约是否过期
    public boolean isExpired() {
        return isExpired(0l);
    }

    public boolean isExpired(long additionalLeaseMs) {
        return (evictionTimestamp > 0 || System.currentTimeMillis() > (lastUpdateTimestamp + duration + additionalLeaseMs));
    }

    public T getHolder() {
        return holder;
    }

}
```

`Lease`中的定义了租约的操作操作类型，分别是注册、下线、更新，同时具备对租约中时间属性进行的各项操作。默认的租约有效时间`duration`为90秒。

其中`AbstractInstanceRegistry`中了对上述方法的进行了实现，有兴趣的同学可以去查看一下源码的实现。

## **Server之间的注册表信息的同步复制**

先介绍一下PeerEurekaNodes，它是管理了Eureka Server的`peer`节点生命周期的列表，其中`peer`的信息封装在`PeerEurekaNode`类中管理了Eureka Server的`peer`节点生命周期的列表，简单理解，一个`PeerEurekaNode`就是一个Eureka Server集群的节点。

在`PeerAwareInstanceRegistryImpl`中，对`Abstractinstanceregistry`中的`register()`、`cancel()`、`renew()`等方法都添加了同步到`PeerEurekaNode`的操作，使Server集群中的注册表信息保持最终一致性。

```javascript
@Override
public boolean cancel(final String appName, final String id, final boolean isReplication) {
    if (super.cancel(appName, id, isReplication)) {
        // 同步下线状态
        replicateToPeers(Action.Cancel, appName, id, null, null, isReplication);
   ...
   }
   ...
}

public void register(final InstanceInfo info, final boolean isReplication) {
    int leaseDuration = Lease.DEFAULT_DURATION_IN_SECS;
    if (info.getLeaseInfo() != null && info.getLeaseInfo().getDurationInSecs() > 0) {
            leaseDuration = info.getLeaseInfo().getDurationInSecs();
     }
    super.register(info, leaseDuration, isReplication);
    // 同步注册状态
       replicateToPeers(Action.Register, info.getAppName(), info.getId(), info, null, isReplication);
}

public boolean renew(final String appName, final String id, final boolean isReplication) {
    if (super.renew(appName, id, isReplication)) {
        // 同步续约状态
        replicateToPeers(Action.Heartbeat, appName, id, null, null, isReplication);
          return true;
    }
      return false;
}
```

同步的状态主要有：

```javascript
public enum Action {
    Heartbeat, Register, Cancel, StatusUpdate, DeleteStatusOverride;
    ...
}
```

对此需要关注的`replicateToPeers()`方法，对传递的不同的同步状态，进行不同的处理。

```javascript
 private void replicateToPeers(Action action, String appName, String id,
                                  InstanceInfo info /* optional */,
                                  InstanceStatus newStatus /* optional */, boolean isReplication) {
    Stopwatch tracer = action.getTimer().start();
    try {
        if (isReplication) {
            numberOfReplicationsLastMin.increment();
          }
          // 如果peer集群为空，或者这本来就是复制操作，那么就不再复制，防止造成循环复制
         if (peerEurekaNodes == Collections.EMPTY_LIST || isReplication) {
           return;
          }
        // 向peer集群中的每一个peer进行同步
        for (final PeerEurekaNode node : peerEurekaNodes.getPeerEurekaNodes()) {
            // 如果peer节点是自身的话，不进行同步复制
            if (peerEurekaNodes.isThisMyUrl(node.getServiceUrl())) {
                continue;
            }
                // 根据Action调用不同的同步请求
                replicateInstanceActionsToPeers(action, appName, id, info, newStatus, node);
         }
     } finally {
           tracer.stop();
      }
}
```

在`replicateInstanceActionsToPeers()`方法中将根据`Action`的不同，调用`PeerEurekaNode`的不同方法进行同步复制。

```javascript
private void replicateInstanceActionsToPeers(Action action, String appName,
                                                 String id, InstanceInfo info, InstanceStatus newStatus,
                                                 PeerEurekaNode node) {
    try {
        InstanceInfo infoFromRegistry = null;
        CurrentRequestVersion.set(Version.V2);
         switch (action) {
           case Cancel:
             node.cancel(appName, id);
             break;
         case Heartbeat:
            InstanceStatus overriddenStatus = overriddenInstanceStatusMap.get(id);
            infoFromRegistry = getInstanceByAppAndId(appName, id, false);
            node.heartbeat(appName, id, infoFromRegistry, overriddenStatus, false);
             break;
        case Register:
            node.register(info);
            break;
        case StatusUpdate:
            infoFromRegistry = getInstanceByAppAndId(appName, id, false);
            node.statusUpdate(appName, id, newStatus, infoFromRegistry);
            break;
        case DeleteStatusOverride:
            infoFromRegistry = getInstanceByAppAndId(appName, id, false);
            node.deleteStatusOverride(appName, id, infoFromRegistry);
            break;
        }
    } catch (Throwable t) {
        logger.error("Cannot replicate information to {} for action {}", node.getServiceUrl(), action.name(), t);
}
```

在`PeerEurekaNode`中的每一个同步复制方式都是通过批任务流的方式进行操作，同时相同的服务实例的相同操作使用相同的任务编号，方便接受的同步复制的Eureka Server根据任务编号的异同合并操作，检查同步操作的数量，减少网络同步的消耗，由于Eureka Server中的信息同步是通过HTTP的方式，难免会出现网络延迟，造成同步复制的延时性，不满足CAP中的C(强一致性)。

## **同步冲突**

对于Eureka Server之间的HTTP以及批任务流交互过程，我们在此不多关注，需要在意的是Eureka Server在接受到对应的同步复制请求后如何修改自身的注册表信息，以及反馈给发起同步复制请求的Eureka Server。

这里首先明确一个概念，`InstanceInfo`中的`lastDirtyTimestamp`表示的是服务实例信息的上次变动的时间戳，可以比较它来了解服务实例信息的哪边更新。

考虑以下的情况，在Euerka Server同步的过程如果出现同一服务实例在两个Server的信息不一致的信息冲突，将如何进行处理？主要有以下两种情况：

- 同步注册信息的时候，被同步的一方也同样存在相同服务实例的租约，如果被同步一方的`lastDirtyTimestamp`比较小，那么被同步一方的注册表中关于该服务实例的租约将会被覆，如果被同步的一方的`lastDirtyTimestamp`的比较大，那么租约将不会被覆盖，(这部分在`AbstractInstanceRegistry.register()`中代码中查看答案)但是这时发起同步的Eureka Server中的租约就是`dirty`的，该如何处理？(问题1)
- 同步续约(心跳)信息的时候，被同步一方的租约不存在或者是`lastDirtyTimestamp`比较小(问题2)(被同步一方的租约是`dirty`)，如何处理；
- 或者被同步一方的`lastDirtyTimestamp`比较大，又如何处理？(问题3)(发起同步的一方的租约是`dirty`)

这是总共是3个问题，让我们在下面一一解答。

不考虑`cancel()`的同步情况，是因为这不会对Eureka Server集群中的注册表信息造成污染，由于各Eureka Server中有自身的定时租约剔除操作(`evict()`)

首先我们看一下`InstanceResource`，这不仅是Eureka Client与Eureka Server进行通信的endpoint，同时也是Eureka Server与Eureka Server之间进行同步复制的进行处理的委托类。

在`InstanceResource`中我们主要关注`renewLease()`，是用于Eureka Client请求该接口维持在Eureka Server中的注册表中的租约，就是维持心跳的接口

```javascript
public Response renewLease(
    @HeaderParam(PeerEurekaNode.HEADER_REPLICATION) String isReplication,
    @QueryParam("overriddenstatus") String overriddenStatus,
    @QueryParam("status") String status,
    @QueryParam("lastDirtyTimestamp") String lastDirtyTimestamp) {
    boolean isFromReplicaNode = "true".equals(isReplication);
    boolean isSuccess = registry.renew(app.getName(), id, isFromReplicaNode);

    // 没有发现对应的租约，要求一次注册
    if (!isSuccess) {
        logger.warn("Not Found (Renew): {} - {}", app.getName(), id);
        return Response.status(Status.NOT_FOUND).build();
    }
    // 是否需要同步数据到发起同步方，因为本地的服务实例信息更新
    Response response = null;
    if (lastDirtyTimestamp != null && serverConfig.shouldSyncWhenTimestampDiffers()) {
        // 验证本地的注册表中的服务实例的lastDirtyTimestamp是不是更小，如果是返回404
        response = this.validateDirtyTimestamp(Long.valueOf(lastDirtyTimestamp), isFromReplicaNode);
        ... 
        }
    } else {
        response = Response.ok().build();
    }
    logger.debug("Found (Renew): {} - {}; reply status={}" + app.getName(), id, response.getStatus());
    return response;
}
```

在`AbstractInstanceRegistry.renew()`方法中，返回false的情况只有两种，一种是租约确实不存在，另一种是`overriddenInstanceStatus`，表示无法续约，这是时候将返回status为404给请求端，同时`renewLease()`方法调用了`validateDirtyTimestamp()`方法判断本地注册表中服务实例的`lastDirtyTimestamp`与续租时传递的`lastDirtyTimestamp`进行比较，如果本地的比较小，一样会返回404的status，相反如果本地的比较大，就返回409的status，同时将本地的`InstanceInfo`放到repsonse中，将这就符合了我们的问题2和问题3的情况。

```javascript
// validateDirtyTimestamp()方法中
 ...
if (lastDirtyTimestamp > appInfo.getLastDirtyTimestamp()) {
    // 本地注册表中的服务实例的lastDirtyTimestamp比较小
    return Response.status(Status.NOT_FOUND).build();
} else if (appInfo.getLastDirtyTimestamp() > lastDirtyTimestamp) {
   // 本地注册表中的服务实例的lastDirtyTimestamp比较大
    if (isReplication) {
    // 如果在同步复制情况下，返回409，同时将本地的InstanceInfo放到response中
    return Response.status(Status.CONFLICT).entity(appInfo).build();
    } else {
        return Response.ok().build();
    }
}
...
```

接着我们跟踪到`PeerReplicationResource`，这里是Eureka Server之间进行同步复制的endpoint，我们找到`handleHeartbeat()`方法。

```javascript
private static Builder handleHeartbeat(EurekaServerConfig config, InstanceResource resource, String lastDirtyTimestamp, String overriddenStatus, String instanceStatus) {
    Response response = resource.renewLease(REPLICATION, overriddenStatus, instanceStatus, lastDirtyTimestamp);
    int responseStatus = response.getStatus();
    Builder responseBuilder = new Builder().setStatusCode(responseStatus);

    if ("false".equals(config.getExperimental("bugfix.934"))) {
        if (responseStatus == Status.OK.getStatusCode() && response.getEntity() != null) {
            responseBuilder.setResponseEntity((InstanceInfo) response.getEntity());
        }
    } else {
        // 如果检测到
         if ((responseStatus == Status.OK.getStatusCode() || responseStatus == Status.CONFLICT.getStatusCode())
                    && response.getEntity() != null) {
            responseBuilder.setResponseEntity((InstanceInfo) response.getEntity());     }
    }
        return responseBuilder;
}
```

处理方式没有多大的变化，虽然对重新构建的Response，但是和上面的返回结果是一致的。

现在我们就可以假设一下问题2和问题3的是如何解决的：

- 如果是被同步一方Eureka Server的该服务实例的租约不存在或者是`lastDirtyTimestamp`比较小，那么它将在设置返回的response status为404；发起同步的一方会将这个服务实例的信息通过同步注册的方式再次发送。在Eureka Client与Eureka Server之间的续租(心跳)就是这样一个流程
- 如果被同步一方Eureka Server的该服务实例的租约的`lastDirtyTimestamp`比较大，那么它将在设置返回的response status为409，同时将本地的该服务实例的`InstanceInfo`发到response中；发起同步的一方会将根据409的状态，抽取出response中的`InstanceInfo`，将其注册到本地注册表中

以上都还只是我们的猜想，需要我们进行验证。

找到`ReplicationTaskProcessor`类，这是对同步复制批任务流处理的类，Eureka Server在该类中发起与peer节点的之间的HTTP同步请求，并对返回的response进行处理。

在这里，我们发现上述单任务流同步操作，还是批任务流同步操作，在处理结果上都是调用了`ReplicationTask`中的方法，`ReplicationTask`类代表的是单个同步复制任务

```javascript
public void handleSuccess() {
}
public void handleFailure(int statusCode, Object responseEntity) throws Throwable {
    logger.warn("The replication of task {} failed with response code {}", getTaskName(), statusCode);
}
```

但是这里并没有我们猜想中的处理，但是我们发现`ReplicationTask`是一个`abstract`，说明底下肯定有其他实现了。

最后，我们回到`PeerEurekaNode`中，在创建每个`ReplicationTask`任务的地方，我们发现对`handleFailure()`方法的重写。

```javascript
// PeerEurekaNode
public void heartbeat(final String appName, final String id, final InstanceInfo info, final InstanceStatus overriddenStatus, boolean primeConnection) throws Throwable {
    ...
    ReplicationTask replicationTask = new InstanceReplicationTask(targetHost, Action.Heartbeat, info, overriddenStatus, false) {
        @Override
        public EurekaHttpResponse<InstanceInfo> execute() throws Throwable {
            return replicationClient.sendHeartBeat(appName, id, info, overriddenStatus);
        }

        @Override
        public void handleFailure(int statusCode, Object responseEntity) throws Throwable {
        super.handleFailure(statusCode, responseEntity);
            if (statusCode == 404) {
                logger.warn("{}: missing entry.", getTaskName());
                if (info != null) {
                    // 如果状态是404，发起一次同步注册
                    register(info);
                }
            } else if (config.shouldSyncWhenTimestampDiffers()) {
                InstanceInfo peerInstanceInfo = (InstanceInfo) responseEntity;
                if (peerInstanceInfo != null) {
                    // 如果两者的lastDirtyTimestamp，同步response中的InstanceInfo到本地
                    syncInstancesIfTimestampDiffers(appName, id, info, peerInstanceInfo);
                }
            }
        }
    };
    long expiryTime = System.currentTimeMillis() + getLeaseRenewalOf(info);
    // 提交任务到批分发器中
    batchingDispatcher.process(taskId("heartbeat", info), replicationTask, expiryTime);
}


private void syncInstancesIfTimestampDiffers(String appName, String id, InstanceInfo info, InstanceInfo infoFromPeer) {
    try {
        if (infoFromPeer != null) {
            if (infoFromPeer.getOverriddenStatus() != null && !InstanceStatus.UNKNOWN.equals(infoFromPeer.getOverriddenStatus())) {          
              registry.storeOverriddenStatusIfRequired(appName, id, infoFromPeer.getOverriddenStatus());
             }
             // 将InstanceInfo注册到本地，覆盖本地注册表中服务实例信息
            registry.register(infoFromPeer, true);
       }
    } catch (Throwable e) {
        logger.warn("Exception when trying to set information from peer :", e);
      }
}
```

通过上面的代码，我们最终发现了完整的闭环操作，与我们所做的猜想是一样的。

但是问题1又如何解决呢？厉害的读者一定也猜到了，没错，还是通过续租(心跳)同步，当Eureka Client与Eureka Server发起`renew()`请求的时候，接受`renew()`将持有最新的`lastDirtyTimestamp`，通过同步心跳(续租)的方式，将该服务实例的最新`InstanceInfo`同步覆盖到peer节点的注册表中，维持Server集群注册表信息的一致性。

所以，我们发现整一个Eureka Server的集群是通过续租(心跳)的操作来维持集群的注册表信息的最终一致性，但是由于网络延迟或者波动原因，无法做到强一致性。