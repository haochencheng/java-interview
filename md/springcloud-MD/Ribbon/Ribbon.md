### Ribbon原理

Ribbon is a client side IPC library that is battle-tested in cloud. It provides the following features

- Load balancing
- Fault tolerance
- Multiple protocol (HTTP, TCP, UDP) support in an asynchronous and reactive model
- Caching and batching



###	LoadBalancing 负载均衡

ribbon的负载均衡通过一个接口定义ILoadBalancer，负载均衡就是从一组服务列表中选一个服务进行调用。

```java
package com.netflix.loadbalancer;

import java.util.List;

/**
 * 定义软件负载均衡器操作的接口,典型的loadbalancer至少需要一组服务器来进行负载平衡,
 * 一个方法标记一个特定的服务器不可用（下线），
 * 一个调用方法从现有的服务器列表中选择一个服务器进行调用。
 * Interface that defines the operations for a software loadbalancer. A typical
 * loadbalancer minimally need a set of servers to loadbalance for, a method to
 * mark a particular server to be out of rotation and a call that will choose a
 * server from the existing list of server.
 * 
 * @author stonse
 * 
 */
public interface ILoadBalancer {

	/**
	 * 初始化服务列表
	 * 这个API还可以在稍后添加其他API
	 * 可以多次添加相同的逻辑服务器(主机:port)，加权重的时候回用到
	 * Initial list of servers.
	 * This API also serves to add additional ones at a later time
	 * The same logical server (host:port) could essentially be added multiple times
	 * (helpful in cases where you want to give more "weightage" perhaps ..)
	 * 
	 * @param newServers new servers to add
	 */
	public void addServers(List<Server> newServers);
	
	/**
	 * 从负载均衡器中选择一个服务
	 * Choose a server from load balancer.
	 * 
	 * @param key An object that the load balancer may use to determine which server to return. null if 
	 *         the load balancer does not use this parameter.
	 * @return server chosen
	 */
	public Server chooseServer(Object key);
	
	/**
	 * 客户端通知服务已经down，否则LB仍然认为服务可用直到下一次ping周期 （假设LB实现会发送Ping。。 ）
	 * To be called by the clients of the load balancer to notify that a Server is down
	 * else, the LB will think its still Alive until the next Ping cycle - potentially
	 * (assuming that the LB Impl does a ping)
	 * 
	 * @param server Server to mark as down
	 */
	public void markServerDown(Server server);
	
	/**
	 * 返回可用的服务列表
	 * @return Only the servers that are up and reachable.
     */
    public List<Server> getReachableServers();

    /**
     * 返回全部的服务列表
     * @return All known servers, both reachable and unreachable.
     */
	public List<Server> getAllServers();
}

```



负载均衡接口，有4中实现

![负载均衡实现](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/ribbon/IloadBalancer.png)

#####	AbstractLoadBalancer 抽象负载均衡器

将服务分为三个组，全部(ALL)、可用(STATUS_UP)、不可用(STATUS_NOT_UP)

```java
public abstract class AbstractLoadBalancer implements ILoadBalancer {
    
    public enum ServerGroup{
        ALL,
        STATUS_UP,
        STATUS_NOT_UP        
    }
        
    /**
     * delegate to {@link #chooseServer(Object)} with parameter null.
     */
    public Server chooseServer() {
    	return chooseServer(null);
    }

    
    /**
     * List of servers that this Loadbalancer knows about
     * 
     * @param serverGroup Servers grouped by status, e.g., {@link ServerGroup#STATUS_UP}
     */
    public abstract List<Server> getServerList(ServerGroup serverGroup);
    
    /**
     * Obtain LoadBalancer related Statistics
     */
    public abstract LoadBalancerStats getLoadBalancerStats();    
}
```



#####	BaseLoadBalancer 基础负载均衡器

想下我们如果要实现 负载均衡 、需要 那些 信息。

BaseLoadBalancer中维护了IRule 负载均衡规则/策略





```java
public class BaseLoadBalancer extends AbstractLoadBalancer implements
        PrimeConnections.PrimeConnectionListener, IClientConfigAware {

		
		
		 //根据key 获取可用的服务
		 public Server chooseServer(Object key) {
        if (counter == null) {
            counter = createCounter();
        }
        counter.increment();
        if (rule == null) {
            return null;
        } else {
            try {
                return rule.choose(key);
            } catch (Exception e) {
                logger.warn("LoadBalancer [{}]:  Error choosing server for key {}", name, key, e);
                return null;
            }
        }
    }

}
```





#####	IRule 负载均衡规则/策略

维护了一个ILoadBalancer 

提供一个获取服务的接口 choose

```java
/**
 * Interface that defines a "Rule" for a LoadBalancer. A Rule can be thought of
 * as a Strategy for loadbalacing. Well known loadbalancing strategies include
 * Round Robin, Response Time based etc.
 * 
 * @author stonse
 * 
 */
public interface IRule{
    /*
     * choose one alive server from lb.allServers or
     * lb.upServers according to key
     * 
     * @return choosen Server object. NULL is returned if none
     *  server is available 
     */

    public Server choose(Object key);
    
    public void setLoadBalancer(ILoadBalancer lb);
    
    public ILoadBalancer getLoadBalancer();    
}

```

IRule 有8？种策略

![负载均衡策略](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/ribbon/IRule.png)

#####	AvailabilityFilteringRule 可用的过滤器负载平衡器规则

它过滤掉以下服务器:由于连续连接或读取失败而处于断路器跳行状态，或者有超过可配置限制的活动连接(默认值为Integer.MAX_VALUE),更改此限制的属性*是* ActiveConnectionsLimit 

通过roundRobinRule 循环获取服务，使用 AvailabilityPredicate 判断是否可用 

```java
/**
 * A load balancer rule that filters out servers that:
 * <ul>
 * <li> are in circuit breaker tripped state due to consecutive connection or read failures, or</li>
 * <li> have active connections that exceeds a configurable limit (default is Integer.MAX_VALUE).</li>
 * </ul>
 * The property
 * to change this limit is 
 * <pre>{@code
 * <clientName>.<nameSpace>.ActiveConnectionsLimit
 * }</pre>
 * <p>
 */
public class AvailabilityFilteringRule extends PredicateBasedRule {    

    private AbstractServerPredicate predicate;
    
    public AvailabilityFilteringRule() {
    	super();
    	predicate = CompositePredicate.withPredicate(new AvailabilityPredicate(this, null))
                .addFallbackPredicate(AbstractServerPredicate.alwaysTrue())
                .build();
    }
    
    
    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
    	predicate = CompositePredicate.withPredicate(new AvailabilityPredicate(this, clientConfig))
    	            .addFallbackPredicate(AbstractServerPredicate.alwaysTrue())
    	            .build();
    }

    @Monitor(name="AvailableServersCount", type = DataSourceType.GAUGE)
    public int getAvailableServersCount() {
    	ILoadBalancer lb = getLoadBalancer();
    	List<Server> servers = lb.getAllServers();
    	if (servers == null) {
    		return 0;
    	}
    	return Collections2.filter(servers, predicate.getServerOnlyPredicate()).size();
    }


    /**
     * 重写此方法以提供不遍历*所有服务器的更有效的实现。这是基于以下假设:在大多数情况下，可用的实例*多于不可用的。
     * This method is overridden to provide a more efficient implementation which does not iterate through
     * all servers. This is under the assumption that in most cases, there are more available instances 
     * than not. 
     */
    @Override
    public Server choose(Object key) {
        int count = 0;
      // 使用 RoundRobinRule
      //AvailabilityFilteringRule extends PredicateBasedRule extends ClientConfigEnabledRoundRobinRule ->
      //  RoundRobinRule roundRobinRule = new RoundRobinRule();
   	 // @Override
    //public void initWithNiwsConfig(IClientConfig clientConfig) {
      //  roundRobinRule = new RoundRobinRule();
    //}

        Server server = roundRobinRule.choose(key);
      // 尝试10次 
        while (count++ <= 10) {
          // 如果server 符合 AvailabilityPredicate 规则 返回server
            if (predicate.apply(new PredicateKey(server))) {
                return server;
            }
          //否则 继续寻找下一个server 进行尝试
            server = roundRobinRule.choose(key);
        }
        return super.choose(key);
    }

    @Override
    public AbstractServerPredicate getPredicate() {
        return predicate;
    }
}
```



#####AvailabilityPredicate

#####

```java
/**
 * 谓词的逻辑过滤掉断路器跳闸服务器和服务器与此客户端太多的并发连接。
 * Predicate with the logic of filtering out circuit breaker tripped servers and servers 
 * with too many concurrent connections from this client.
 */
public class AvailabilityPredicate extends  AbstractServerPredicate {
        
    private static final DynamicBooleanProperty CIRCUIT_BREAKER_FILTERING =
            DynamicPropertyFactory.getInstance().getBooleanProperty("niws.loadbalancer.availabilityFilteringRule.filterCircuitTripped", true);

    private static final DynamicIntProperty ACTIVE_CONNECTIONS_LIMIT =
            DynamicPropertyFactory.getInstance().getIntProperty("niws.loadbalancer.availabilityFilteringRule.activeConnectionsLimit", Integer.MAX_VALUE);

    private ChainedDynamicProperty.IntProperty activeConnectionsLimit = new ChainedDynamicProperty.IntProperty(ACTIVE_CONNECTIONS_LIMIT);
        
    public AvailabilityPredicate(IRule rule, IClientConfig clientConfig) {
        super(rule, clientConfig);
        initDynamicProperty(clientConfig);
    }
    
    public AvailabilityPredicate(LoadBalancerStats lbStats, IClientConfig clientConfig) {
        super(lbStats, clientConfig);
        initDynamicProperty(clientConfig);
    }
    
    AvailabilityPredicate(IRule rule) {
        super(rule);
    }

    private void initDynamicProperty(IClientConfig clientConfig) {
        String id = "default";
        if (clientConfig != null) {
            id = clientConfig.getClientName();
            activeConnectionsLimit = new ChainedDynamicProperty.IntProperty(id + "." + clientConfig.getNameSpace() + ".ActiveConnectionsLimit", ACTIVE_CONNECTIONS_LIMIT); 
        }               
    }
    
    @Override
    public boolean apply(@Nullable PredicateKey input) {
        // 获取统计信息
        LoadBalancerStats stats = getLBStats();
        // 如果统计信息为null 说明服务没有被访问过 返回true
        if (stats == null) {
            return true;
        }
       // 返回服务是否可用
      // private final LoadingCache<Server, ServerStats> serverStatsCache 
      // 通过 server 从缓存中取 ServerStats
      // ServerStats 服务统计中记载了 服务的 请求信息 见下文
        return !shouldSkipServer(stats.getSingleServerStat(input.getServer()));
    }
    
    // 如果熔断开启 或者 服务请求数量 > 限制阈值 返回true 否则 false
    private boolean shouldSkipServer(ServerStats stats) {        
        if ((CIRCUIT_BREAKER_FILTERING.get() && stats.isCircuitBreakerTripped()) 
                || stats.getActiveRequestsCount() >= activeConnectionsLimit.get()) {
            return true;
        }
        return false;
    }

}

```



#####	ServerStats 服务统计信息

在LoadBalancer中捕获每个服务器(节点)的各种状态

不同负载均衡规则需要的服务信息 全集

```java
/**
 * Capture various stats per Server(node) in the LoadBalancer
 * @author stonse
 *
 */
public class ServerStats {
    
    private static final int DEFAULT_PUBLISH_INTERVAL =  60 * 1000; // = 1 minute
    private static final int DEFAULT_BUFFER_SIZE = 60 * 1000; // = 1000 requests/sec for 1 minute
    private final DynamicIntProperty connectionFailureThreshold;    
    private final DynamicIntProperty circuitTrippedTimeoutFactor; 
    private final DynamicIntProperty maxCircuitTrippedTimeout;
    private static final DynamicIntProperty activeRequestsCountTimeout = 
        DynamicPropertyFactory.getInstance().getIntProperty("niws.loadbalancer.serverStats.activeRequestsCount.effectiveWindowSeconds", 60 * 10);
    
    private static final double[] PERCENTS = makePercentValues();
    
    private DataDistribution dataDist = new DataDistribution(1, PERCENTS); // in case
    private DataPublisher publisher = null;
    private final Distribution responseTimeDist = new Distribution();
    
    int bufferSize = DEFAULT_BUFFER_SIZE;
    int publishInterval = DEFAULT_PUBLISH_INTERVAL;
    
    
    long failureCountSlidingWindowInterval = 1000; 
    
    private MeasuredRate serverFailureCounts = new MeasuredRate(failureCountSlidingWindowInterval);
    private MeasuredRate requestCountInWindow = new MeasuredRate(300000L);
    
    Server server;
    
    AtomicLong totalRequests = new AtomicLong();
    
    @VisibleForTesting
    AtomicInteger successiveConnectionFailureCount = new AtomicInteger(0);
    
    @VisibleForTesting
    AtomicInteger activeRequestsCount = new AtomicInteger(0);

    @VisibleForTesting
    AtomicInteger openConnectionsCount = new AtomicInteger(0);
    
    private volatile long lastConnectionFailedTimestamp;
    private volatile long lastActiveRequestsCountChangeTimestamp;
    private AtomicLong totalCircuitBreakerBlackOutPeriod = new AtomicLong(0);
    private volatile long lastAccessedTimestamp;
    private volatile long firstConnectionTimestamp = 0;
    
    public ServerStats() {
        connectionFailureThreshold = DynamicPropertyFactory.getInstance().getIntProperty(
                "niws.loadbalancer.default.connectionFailureCountThreshold", 3);        
        circuitTrippedTimeoutFactor = DynamicPropertyFactory.getInstance().getIntProperty(
                "niws.loadbalancer.default.circuitTripTimeoutFactorSeconds", 10);

        maxCircuitTrippedTimeout = DynamicPropertyFactory.getInstance().getIntProperty(
                "niws.loadbalancer.default.circuitTripMaxTimeoutSeconds", 30);
    }
    
    public ServerStats(LoadBalancerStats lbStats) {
        this.maxCircuitTrippedTimeout = lbStats.getCircuitTripMaxTimeoutSeconds();
        this.circuitTrippedTimeoutFactor = lbStats.getCircuitTrippedTimeoutFactor();
        this.connectionFailureThreshold = lbStats.getConnectionFailureCountThreshold();
    }
    
    /**
     * Initializes the object, starting data collection and reporting.
     */
    public void initialize(Server server) {
        serverFailureCounts = new MeasuredRate(failureCountSlidingWindowInterval);
        requestCountInWindow = new MeasuredRate(300000L);
        if (publisher == null) {
            dataDist = new DataDistribution(getBufferSize(), PERCENTS);
            publisher = new DataPublisher(dataDist, getPublishIntervalMillis());
            publisher.start();
        }
        this.server = server;
    }
    
    public void close() {
        if (publisher != null)
            publisher.stop();
    }

    private int getBufferSize() {
        return bufferSize;
    }

    private long getPublishIntervalMillis() {
        return publishInterval;
    }
    
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setPublishInterval(int publishInterval) {
        this.publishInterval = publishInterval;
    }

    /**
     * The supported percentile values.
     * These correspond to the various Monitor methods defined below.
     * No, this is not pretty, but that's the way it is.
     */
    private static enum Percent {

        TEN(10), TWENTY_FIVE(25), FIFTY(50), SEVENTY_FIVE(75), NINETY(90),
        NINETY_FIVE(95), NINETY_EIGHT(98), NINETY_NINE(99), NINETY_NINE_POINT_FIVE(99.5);

        private double val;

        Percent(double val) {
            this.val = val;
        }

        public double getValue() {
            return val;
        }

    }

    private static double[] makePercentValues() {
        Percent[] percents = Percent.values();
        double[] p = new double[percents.length];
        for (int i = 0; i < percents.length; i++) {
            p[i] = percents[i].getValue();
        }
        return p;
    }

    public long getFailureCountSlidingWindowInterval() {
        return failureCountSlidingWindowInterval;
    }

    public void setFailureCountSlidingWindowInterval(
            long failureCountSlidingWindowInterval) {
        this.failureCountSlidingWindowInterval = failureCountSlidingWindowInterval;
    }

    // run time methods
    
    
    /**
     * Increment the count of failures for this Server
     * 
     */
    public void addToFailureCount(){
        serverFailureCounts.increment();
    }
    
    /**
     * Returns the count of failures in the current window
     * 
     */
    public long getFailureCount(){
        return serverFailureCounts.getCurrentCount();
    }
    
    /**
     * Call this method to note the response time after every request
     * @param msecs
     */
    public void noteResponseTime(double msecs){
        dataDist.noteValue(msecs);
        responseTimeDist.noteValue(msecs);
    }
    
    public void incrementNumRequests(){
        totalRequests.incrementAndGet();
    }
    
    public void incrementActiveRequestsCount() {        
        activeRequestsCount.incrementAndGet();
        requestCountInWindow.increment();
        long currentTime = System.currentTimeMillis();
        lastActiveRequestsCountChangeTimestamp = currentTime;
        lastAccessedTimestamp = currentTime;
        if (firstConnectionTimestamp == 0) {
            firstConnectionTimestamp = currentTime;
        }
    }

    public void incrementOpenConnectionsCount() {
        openConnectionsCount.incrementAndGet();
    }

    public void decrementActiveRequestsCount() {
        if (activeRequestsCount.decrementAndGet() < 0) {
            activeRequestsCount.set(0);
        }
        lastActiveRequestsCountChangeTimestamp = System.currentTimeMillis();
    }

    public void decrementOpenConnectionsCount() {
        if (openConnectionsCount.decrementAndGet() < 0) {
            openConnectionsCount.set(0);
        }
    }
    
    public int  getActiveRequestsCount() {
        return getActiveRequestsCount(System.currentTimeMillis());
    }

    public int getActiveRequestsCount(long currentTime) {
        int count = activeRequestsCount.get();
        if (count == 0) {
            return 0;
        } else if (currentTime - lastActiveRequestsCountChangeTimestamp > activeRequestsCountTimeout.get() * 1000 || count < 0) {
            activeRequestsCount.set(0);
            return 0;            
        } else {
            return count;
        }
    }

    public int getOpenConnectionsCount() {
        return openConnectionsCount.get();
    }

    
    public long getMeasuredRequestsCount() {
        return requestCountInWindow.getCount();
    }

    @Monitor(name="ActiveRequestsCount", type = DataSourceType.GAUGE)    
    public int getMonitoredActiveRequestsCount() {
        return activeRequestsCount.get();
    }
    
    @Monitor(name="CircuitBreakerTripped", type = DataSourceType.INFORMATIONAL)    
    public boolean isCircuitBreakerTripped() {
        return isCircuitBreakerTripped(System.currentTimeMillis());
    }
    
    public boolean isCircuitBreakerTripped(long currentTime) {
        long circuitBreakerTimeout = getCircuitBreakerTimeout();
        if (circuitBreakerTimeout <= 0) {
            return false;
        }
        return circuitBreakerTimeout > currentTime;
    }

    
    private long getCircuitBreakerTimeout() {
        long blackOutPeriod = getCircuitBreakerBlackoutPeriod();
        if (blackOutPeriod <= 0) {
            return 0;
        }
        return lastConnectionFailedTimestamp + blackOutPeriod;
    }
    
    private long getCircuitBreakerBlackoutPeriod() {
        int failureCount = successiveConnectionFailureCount.get();
        int threshold = connectionFailureThreshold.get();
        if (failureCount < threshold) {
            return 0;
        }
        int diff = (failureCount - threshold) > 16 ? 16 : (failureCount - threshold);
        int blackOutSeconds = (1 << diff) * circuitTrippedTimeoutFactor.get();
        if (blackOutSeconds > maxCircuitTrippedTimeout.get()) {
            blackOutSeconds = maxCircuitTrippedTimeout.get();
        }
        return blackOutSeconds * 1000L;
    }
    
    public void incrementSuccessiveConnectionFailureCount() {
        lastConnectionFailedTimestamp = System.currentTimeMillis();
        successiveConnectionFailureCount.incrementAndGet();
        totalCircuitBreakerBlackOutPeriod.addAndGet(getCircuitBreakerBlackoutPeriod());
    }
    
    public void clearSuccessiveConnectionFailureCount() {
        successiveConnectionFailureCount.set(0);
    }
    
    
    @Monitor(name="SuccessiveConnectionFailureCount", type = DataSourceType.GAUGE)
    public int getSuccessiveConnectionFailureCount() {
        return successiveConnectionFailureCount.get();
    }
    
    /*
     * Response total times
     */

    /**
     * Gets the average total amount of time to handle a request, in milliseconds.
     */
    @Monitor(name = "OverallResponseTimeMillisAvg", type = DataSourceType.INFORMATIONAL,
             description = "Average total time for a request, in milliseconds")
    public double getResponseTimeAvg() {
        return responseTimeDist.getMean();
    }

    /**
     * Gets the maximum amount of time spent handling a request, in milliseconds.
     */
    @Monitor(name = "OverallResponseTimeMillisMax", type = DataSourceType.INFORMATIONAL,
             description = "Max total time for a request, in milliseconds")
    public double getResponseTimeMax() {
        return responseTimeDist.getMaximum();
    }

    /**
     * Gets the minimum amount of time spent handling a request, in milliseconds.
     */
    @Monitor(name = "OverallResponseTimeMillisMin", type = DataSourceType.INFORMATIONAL,
             description = "Min total time for a request, in milliseconds")
    public double getResponseTimeMin() {
        return responseTimeDist.getMinimum();
    }

    /**
     * Gets the standard deviation in the total amount of time spent handling a request, in milliseconds.
     */
    @Monitor(name = "OverallResponseTimeMillisStdDev", type = DataSourceType.INFORMATIONAL,
             description = "Standard Deviation in total time to handle a request, in milliseconds")
    public double getResponseTimeStdDev() {
        return responseTimeDist.getStdDev();
    }

    /*
     * QOS percentile performance data for most recent period
     */

    /**
     * Gets the number of samples used to compute the various response-time percentiles.
     */
    @Monitor(name = "ResponseTimePercentileNumValues", type = DataSourceType.GAUGE,
             description = "The number of data points used to compute the currently reported percentile values")
    public int getResponseTimePercentileNumValues() {
        return dataDist.getSampleSize();
    }

    /**
     * Gets the time when the varios percentile data was last updated.
     */
    @Monitor(name = "ResponseTimePercentileWhen", type = DataSourceType.INFORMATIONAL,
             description = "The time the percentile values were computed")
    public String getResponseTimePercentileTime() {
        return dataDist.getTimestamp();
    }

    /**
     * Gets the time when the varios percentile data was last updated,
     * in milliseconds since the epoch.
     */
    @Monitor(name = "ResponseTimePercentileWhenMillis", type = DataSourceType.COUNTER,
             description = "The time the percentile values were computed in milliseconds since the epoch")
    public long getResponseTimePercentileTimeMillis() {
        return dataDist.getTimestampMillis();
    }

    /**
     * Gets the average total amount of time to handle a request
     * in the recent time-slice, in milliseconds.
     */
    @Monitor(name = "ResponseTimeMillisAvg", type = DataSourceType.GAUGE,
             description = "Average total time for a request in the recent time slice, in milliseconds")
    public double getResponseTimeAvgRecent() {
        return dataDist.getMean();
    }
    
    /**
     * Gets the 10-th percentile in the total amount of time spent handling a request, in milliseconds.
     */
    @Monitor(name = "ResponseTimeMillis10Percentile", type = DataSourceType.INFORMATIONAL,
             description = "10th percentile in total time to handle a request, in milliseconds")
    public double getResponseTime10thPercentile() {
        return getResponseTimePercentile(Percent.TEN);
    }

    /**
     * Gets the 25-th percentile in the total amount of time spent handling a request, in milliseconds.
     */
    @Monitor(name = "ResponseTimeMillis25Percentile", type = DataSourceType.INFORMATIONAL,
             description = "25th percentile in total time to handle a request, in milliseconds")
    public double getResponseTime25thPercentile() {
        return getResponseTimePercentile(Percent.TWENTY_FIVE);
    }

    /**
     * Gets the 50-th percentile in the total amount of time spent handling a request, in milliseconds.
     */
    @Monitor(name = "ResponseTimeMillis50Percentile", type = DataSourceType.INFORMATIONAL,
             description = "50th percentile in total time to handle a request, in milliseconds")
    public double getResponseTime50thPercentile() {
        return getResponseTimePercentile(Percent.FIFTY);
    }

    /**
     * Gets the 75-th percentile in the total amount of time spent handling a request, in milliseconds.
     */
    @Monitor(name = "ResponseTimeMillis75Percentile", type = DataSourceType.INFORMATIONAL,
             description = "75th percentile in total time to handle a request, in milliseconds")
    public double getResponseTime75thPercentile() {
        return getResponseTimePercentile(Percent.SEVENTY_FIVE);
    }

    /**
     * Gets the 90-th percentile in the total amount of time spent handling a request, in milliseconds.
     */
    @Monitor(name = "ResponseTimeMillis90Percentile", type = DataSourceType.INFORMATIONAL,
             description = "90th percentile in total time to handle a request, in milliseconds")
    public double getResponseTime90thPercentile() {
        return getResponseTimePercentile(Percent.NINETY);
    }

    /**
     * Gets the 95-th percentile in the total amount of time spent handling a request, in milliseconds.
     */
    @Monitor(name = "ResponseTimeMillis95Percentile", type = DataSourceType.GAUGE,
             description = "95th percentile in total time to handle a request, in milliseconds")
    public double getResponseTime95thPercentile() {
        return getResponseTimePercentile(Percent.NINETY_FIVE);
    }

    /**
     * Gets the 98-th percentile in the total amount of time spent handling a request, in milliseconds.
     */
    @Monitor(name = "ResponseTimeMillis98Percentile", type = DataSourceType.INFORMATIONAL,
             description = "98th percentile in total time to handle a request, in milliseconds")
    public double getResponseTime98thPercentile() {
        return getResponseTimePercentile(Percent.NINETY_EIGHT);
    }

    /**
     * Gets the 99-th percentile in the total amount of time spent handling a request, in milliseconds.
     */
    @Monitor(name = "ResponseTimeMillis99Percentile", type = DataSourceType.GAUGE,
             description = "99th percentile in total time to handle a request, in milliseconds")
    public double getResponseTime99thPercentile() {
        return getResponseTimePercentile(Percent.NINETY_NINE);
    }

    /**
     * Gets the 99.5-th percentile in the total amount of time spent handling a request, in milliseconds.
     */
    @Monitor(name = "ResponseTimeMillis99_5Percentile", type = DataSourceType.GAUGE,
             description = "99.5th percentile in total time to handle a request, in milliseconds")
    public double getResponseTime99point5thPercentile() {
        return getResponseTimePercentile(Percent.NINETY_NINE_POINT_FIVE);
    }

    public long getTotalRequestsCount() {
        return totalRequests.get();
    }
    
    private double getResponseTimePercentile(Percent p) {
        return dataDist.getPercentiles()[p.ordinal()];
    }
    
    //toString 方法中输出了统计的信息
    //eg.ServerStats:[Server:stonse:80;	Zone:UNKNOWN;	Total Requests:99;	Successive connection failure:0;	Total blackout seconds:0;	Last connection made:Thu Jan 01 08:00:00 CST 1970;	First connection made: Thu Jan 01 08:00:00 CST 1970;	Active Connections:0;	total failure count in last (1000) msecs:0;	average resp time:13.275308970139484;	90 percentile resp time:23.14859407430382;	95 percentile resp time:23.14859407430382;	min resp time:0.021071074712381233;	max resp time:24.60484318031518;	stddev resp time:7.121850590230442]
    // Zone 区
    // Total Requests: 总请求数
    // Successive connection failure:连续的连接失败
    // Blackout until: 熔断开始时间
    // Total blackout seconds:总断路器断电时间
    // Last connection failure:上次连接失败的时间戳
    // First connection made: 第一次连接时间戳
    // Active Connections:被监视的活动请求计数
    // total failure count in last (" + failureCountSlidingWindowInterval + ") msecs:在故障计数滑动窗口间隔的总故障计数
    // average resp time:平均响应时间
    // 90 percentile resp time:响应时间的90%
    // 95 percentile resp time:响应时间的95%
    // min resp time:最小响应时间
    // max resp time:最大响应时间
    // stddev resp time:Std开发响应时间
    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        sb.append("[Server:" + server + ";");
        sb.append("\tZone:" + server.getZone() + ";");
        sb.append("\tTotal Requests:" + totalRequests + ";");
        sb.append("\tSuccessive connection failure:" + getSuccessiveConnectionFailureCount() + ";");
        if (isCircuitBreakerTripped()) {
            sb.append("\tBlackout until: " + new Date(getCircuitBreakerTimeout()) + ";");
        }
        sb.append("\tTotal blackout seconds:" + totalCircuitBreakerBlackOutPeriod.get() / 1000 + ";");
        sb.append("\tLast connection made:" + new Date(lastAccessedTimestamp) + ";");
        if (lastConnectionFailedTimestamp > 0) {
            sb.append("\tLast connection failure: " + new Date(lastConnectionFailedTimestamp)  + ";");
        }
        sb.append("\tFirst connection made: " + new Date(firstConnectionTimestamp)  + ";");
        sb.append("\tActive Connections:" + getMonitoredActiveRequestsCount()  + ";");
        sb.append("\ttotal failure count in last (" + failureCountSlidingWindowInterval + ") msecs:" + getFailureCount()  + ";");
        sb.append("\taverage resp time:" + getResponseTimeAvg()  + ";");
        sb.append("\t90 percentile resp time:" + getResponseTime90thPercentile()  + ";");
        sb.append("\t95 percentile resp time:" + getResponseTime95thPercentile()  + ";");
        sb.append("\tmin resp time:" + getResponseTimeMin()  + ";");
        sb.append("\tmax resp time:" + getResponseTimeMax()  + ";");
        sb.append("\tstddev resp time:" + getResponseTimeStdDev());
        sb.append("]\n");
        
        return sb.toString();
    }
   
}
```





#####	RoundRobinRule 循环负载均衡

最广为人知和最基本的负载平衡策略，即循环规则。

```java
/**
 * The most well known and basic load balancing strategy, i.e. Round Robin Rule.
 *
 * @author stonse
 * @author Nikos Michalakis <nikos@netflix.com>
 *
 */
public class RoundRobinRule extends AbstractLoadBalancerRule {

    private AtomicInteger nextServerCyclicCounter;
    private static final boolean AVAILABLE_ONLY_SERVERS = true;
    private static final boolean ALL_SERVERS = false;

    private static Logger log = LoggerFactory.getLogger(RoundRobinRule.class);

    public RoundRobinRule() {
        nextServerCyclicCounter = new AtomicInteger(0);
    }

    public RoundRobinRule(ILoadBalancer lb) {
        this();
        setLoadBalancer(lb);
    }

    public Server choose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            log.warn("no load balancer");
            return null;
        }

        Server server = null;
        int count = 0;
        while (server == null && count++ < 10) {
            List<Server> reachableServers = lb.getReachableServers();
            List<Server> allServers = lb.getAllServers();
            int upCount = reachableServers.size();
            int serverCount = allServers.size();

            if ((upCount == 0) || (serverCount == 0)) {
                log.warn("No up servers available from load balancer: " + lb);
                return null;
            }

            int nextServerIndex = incrementAndGetModulo(serverCount);
            server = allServers.get(nextServerIndex);

            if (server == null) {
                /* Transient. */
                Thread.yield();
                continue;
            }

            if (server.isAlive() && (server.isReadyToServe())) {
                return (server);
            }

            // Next.
            server = null;
        }

        if (count >= 10) {
            log.warn("No available alive servers after 10 tries from load balancer: "
                    + lb);
        }
        return server;
    }

    /**
     * cas 实现获取下一个服务器
     * Inspired by the implementation of {@link AtomicInteger#incrementAndGet()}.
     *
     * @param modulo The modulo to bound the value of the counter.
     * @return The next value.
     */
    private int incrementAndGetModulo(int modulo) {
        for (;;) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next))
                return next;
        }
    }

    @Override
    public Server choose(Object key) {
        return choose(getLoadBalancer(), key);
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
    }
}
```









###	Fault tolerance 容错



### Multiple protocol (HTTP, TCP, UDP) support in an asynchronous and reactive model

多协议 支持在 异步和响应式编程



###	Caching and batching 缓存和批处理







### 









