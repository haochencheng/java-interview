###	Eureka-server

@EnableEurekaServer使用@Import导入配置类

EurekaServerMarkerConfiguration

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EurekaServerMarkerConfiguration.class)
public @interface EnableEurekaServer {
	
}
```

EurekaServerMarkerConfiguration

一个标记类，用作自动装配条件。

```java
@Configuration
public class EurekaServerMarkerConfiguration {

	@Bean
	public Marker eurekaServerMarkerBean() {
		return new Marker();
	}

	class Marker {
	}
}
```

eureka-server包中spring.factories

```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  org.springframework.cloud.netflix.eureka.server.EurekaServerAutoConfiguration
```

EurekaServerAutoConfiguration 中使用@Import导入EurekaServerInitializerConfiguration

```
@Configuration
@Import(EurekaServerInitializerConfiguration.class)
@ConditionalOnBean(EurekaServerMarkerConfiguration.Marker.class)
@EnableConfigurationProperties({ EurekaDashboardProperties.class,
      InstanceRegistryProperties.class })
@PropertySource("classpath:/eureka/server.properties")
public class EurekaServerAutoConfiguration extends WebMvcConfigurerAdapter {
```

####	EurekaServerInitializerConfiguration

EurekaServerInitializerConfiguration实现了spring的Lifecycle接口，

在spring启时启动。

```java
@Configuration
public class EurekaServerInitializerConfiguration
		implements ServletContextAware, SmartLifecycle, Ordered {
  
			@Override
	public void start() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					//TODO: is this class even needed now?
					eurekaServerBootstrap.contextInitialized(EurekaServerInitializerConfiguration.this.servletContext);
					log.info("Started Eureka Server");

					publish(new EurekaRegistryAvailableEvent(getEurekaServerConfig()));
					EurekaServerInitializerConfiguration.this.running = true;
					publish(new EurekaServerStartedEvent(getEurekaServerConfig()));
				}
				catch (Exception ex) {
					// Help!
					log.error("Could not initialize Eureka servlet context", ex);
				}
			}
		}).start();
	}

}
```



1. 像附近节点同步数据
2. 启动定时任务过期任务
3. 注册节点信息

```java
public class EurekaServerBootstrap {
		public void contextInitialized(ServletContext context) {
		try {
			initEurekaEnvironment();
			initEurekaServerContext();

			context.setAttribute(EurekaServerContext.class.getName(), this.serverContext);
		}
		catch (Throwable e) {
			log.error("Cannot bootstrap eureka server :", e);
			throw new RuntimeException("Cannot bootstrap eureka server :", e);
		}
	}
  
protected void initEurekaServerContext() throws Exception {
		EurekaServerContextHolder.initialize(this.serverContext);

		log.info("Initialized server context");

		// Copy registry from neighboring eureka node
		int registryCount = this.registry.syncUp();
		this.registry.openForTraffic(this.applicationInfoManager, registryCount);

		// Register all monitoring statistics.
		EurekaMonitors.registerAllStats();
	}
  
}
```



###	Eureka-client

通过@EnableDiscoveryClient或者@EnableEurekaClient注解实现自动注册。

```java
/**
 * Annotation to enable a DiscoveryClient implementation.
 * @author Spencer Gibb
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(EnableDiscoveryClientImportSelector.class)
public @interface EnableDiscoveryClient {

	/**
	 * If true, the ServiceRegistry will automatically register the local server.
	 */
	boolean autoRegister() default true;
}
```



EnableDiscoveryClient通过@Import导入(EnableDiscoveryClientImportSelector.class)

加载AutoServiceRegistrationConfiguration

通过@EnableConfigurationProperties

导入AutoServiceRegistrationProperties

#####	初始化定时任务

1. 心跳定时任务
2. 刷新注册缓存定时任务

#####	获取服务/服务发现

Eureka客户端启动时从服务端获取注册的服务，存在本地缓存中。后台定时任务周期性刷新缓存。

```java
 if (clientConfig.shouldFetchRegistry()) {
            // registry cache refresh timer
            int registryFetchIntervalSeconds = clientConfig.getRegistryFetchIntervalSeconds();
            int expBackOffBound = clientConfig.getCacheRefreshExecutorExponentialBackOffBound();
            scheduler.schedule(
                    new TimedSupervisorTask(
                            "cacheRefresh",
                            scheduler,
                            cacheRefreshExecutor,
                            registryFetchIntervalSeconds,
                            TimeUnit.SECONDS,
                            expBackOffBound,
                            new CacheRefreshThread()
                    ),
                    registryFetchIntervalSeconds, TimeUnit.SECONDS);
        }
```

#####	

定时任务服务发现

DiscoveryClient中，服务启动或者本地服务列表为空，进行全量同步。

增量同步失败（同步完成本地hashcode和服务端hashcode不一致）采用全量同步。

```java
class CacheRefreshThread implements Runnable {
        public void run() {
            refreshRegistry();
        }
    }
    
      boolean success = fetchRegistry(remoteRegionsModified);
      
      
private boolean fetchRegistry(boolean forceFullRegistryFetch) {
        Stopwatch tracer = FETCH_REGISTRY_TIMER.start();

        try {
            // If the delta is disabled or if it is the first time, get all
            // applications
            Applications applications = getApplications();

            if (clientConfig.shouldDisableDelta()
                    || (!Strings.isNullOrEmpty(clientConfig.getRegistryRefreshSingleVipAddress()))
                    || forceFullRegistryFetch
                    || (applications == null)
                    || (applications.getRegisteredApplications().size() == 0)
                    || (applications.getVersion() == -1)) //Client application does not have latest library supporting delta
            {
       		// 全量同步
                getAndStoreFullRegistry();
            } else {
              // 增量同步
                getAndUpdateDelta(applications);
            }
 

        // Notify about cache refresh before updating the instance remote status
        onCacheRefreshed();

        // Update remote status based on refreshed data held in the cache
        updateInstanceRemoteStatus();

        // registry was fetched successfully, so return true
        return true;
    }
      
```



### 	租约

Eureka对租约的管理和集群同步通过广播的方式（peer to peer 对等网络），通过继承的方式实现。

```
InstanceRegistry => PeerAwareInstanceRegistryImpl => AbstractInstanceRegistry
```

InstanceRegistry 发布事件

PeerAwareInstanceRegistryImpl 集群同步处理类

AbstractInstanceRegistry 具体的处理类

流程为本地先处理 注册、续约、下线 操作。然后在广播到集群中。

操作分为是否是复制， 通过boolean isReplication区分（header中）。

如果是复制操作则停止广播。

####	LeaseManager

在LeaseManager接口中定义了关于Lease（租约）的管理。

```java
public interface LeaseManager<T> {

    /**
     * Assign a new {@link Lease} to the passed in {@link T}.
     *
     * @param r
     *            - T to register
     * @param leaseDuration
     * @param isReplication
     *            - whether this is a replicated entry from another eureka node.
     */
    void register(T r, int leaseDuration, boolean isReplication);

    /**
     * Cancel the {@link Lease} associated w/ the passed in <code>appName</code>
     * and <code>id</code>.
     *
     * @param appName
     *            - unique id of the application.
     * @param id
     *            - unique id within appName.
     * @param isReplication
     *            - whether this is a replicated entry from another eureka node.
     * @return true, if the operation was successful, false otherwise.
     */
    boolean cancel(String appName, String id, boolean isReplication);

    /**
     * Renew the {@link Lease} associated w/ the passed in <code>appName</code>
     * and <code>id</code>.
     *
     * @param id
     *            - unique id within appName
     * @param isReplication
     *            - whether this is a replicated entry from another ds node
     * @return whether the operation of successful
     */
    boolean renew(String appName, String id, boolean isReplication);

    /**
     * Evict {@link T}s with expired {@link Lease}(s).
     */
    void evict();
}

```

####	Lease（租约类）

```java
public class Lease<T> {

    enum Action {
      // 注册，取消，续约
        Register, Cancel, Renew
    };
    
   public static final int DEFAULT_DURATION_IN_SECS = 90;

    private T holder;
    private long evictionTimestamp;
    private long registrationTimestamp;
    private long serviceUpTimestamp;
    // Make it volatile so that the expiration task would see this quicker
    private volatile long lastUpdateTimestamp;
    private long duration;
  
}    
```

#####	服务注册





#####	心跳检测/服务续约

```java
 // Heartbeat timer
            scheduler.schedule(
                    new TimedSupervisorTask(
                            "heartbeat",
                            scheduler,
                            heartbeatExecutor,
                            renewalIntervalInSecs,
                            TimeUnit.SECONDS,
                            expBackOffBound,
                            new HeartbeatThread()
                    ),
                    renewalIntervalInSecs, TimeUnit.SECONDS);
```

Eureka客户端向服务端发送心跳续约，超过服务过期时长没有收到心跳标记服务下线。

```properties
# 续约定时任务周期
eureka.instance.lease-renewal-interval-in-seconds=30
#	服务失效时长
eureka.instance.lease-expiration-duration-in-seconds=90
```



#####	服务主动下线（shutdown）/服务剔除（evit）

1. 客户端调用 下线方法

- 2.1客户端下线

```java
public class DiscoveryClient implements EurekaClient {
  
 /**
     * Shuts down Eureka Client. Also sends a deregistration request to the
     * eureka server.
     */  
@PreDestroy
    @Override
    public synchronized void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            logger.info("Shutting down DiscoveryClient ...");

            if (statusChangeListener != null && applicationInfoManager != null) {
                
//取消监听
              applicationInfoManager.unregisterStatusChangeListener(statusChangeListener.getId());
            }
//取消定时任务 1.心跳 2.同步服务端 注册信息
            cancelScheduledTasks();

            // If APPINFO was registered
            if (applicationInfoManager != null
                    && clientConfig.shouldRegisterWithEureka()
                    && clientConfig.shouldUnregisterOnShutdown()) {
              //标记服务下线
                applicationInfoManager.setInstanceStatus(InstanceStatus.DOWN);
              //向服务端取消注册
                unregister();
            }

            if (eurekaTransport != null) {
                eurekaTransport.shutdown();
            }

            heartbeatStalenessMonitor.shutdown();
            registryStalenessMonitor.shutdown();

            logger.info("Completed shut down of DiscoveryClient");
        }
    }
}
```



2. 服务端清除过期客户端

- 2.1 如何判断是否过期

首先对 Lease 几个重要属性进行说明：

```java
private long evictionTimestamp;		// 服务下线时间
private long registrationTimestamp;	// 服务注册时间
private long serviceUpTimestamp;	// 服务UP时间
private volatile long lastUpdateTimestamp;	// 最后一次心跳续约时间
private long duration;				// 心跳过期时间，默认 90s
```

Lease 每次心跳续约时都会更新最后一次续约时间 lastUpdateTimestamp。如果服务下线则会更新下线时间 evictionTimestamp，这样 evictionTimestamp > 0 就表示服务已经下线了。默认心跳续约时间超过 90s 服务就自动过期。

```java
public boolean isExpired(long additionalLeaseMs) {
    return (evictionTimestamp > 0 || System.currentTimeMillis() > 
            (lastUpdateTimestamp + duration + additionalLeaseMs));
}
```

**总结：** additionalLeaseMs 是一种补偿机制，可以当成默认值 0ms。

#####	相关博客

https://www.cnblogs.com/binarylei/p/11563952.html#eureka