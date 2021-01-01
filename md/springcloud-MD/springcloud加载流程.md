###	SpringCloud启动加载流程

spirngcloud通过监听spring（ApplicationEnvironmentPreparedEvent）事件完成启动加载bootstrap.properties/yml配置文件。

```java
public class BootstrapApplicationListener
		implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

  public static final String BOOTSTRAP_PROPERTY_SOURCE_NAME = "bootstrap";

@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {


}
```

1. 判断是否开启springcloud

- environment中属性spring.cloud.bootstrap.enabled是否开启

-  environment中是否包含bootstrap配置文件，如果包含就返回。防止重复加载。

2. 加载、解析bootstrap配置文件

- 解析${spring.cloud.bootstrap.name:bootstrap}



Spirngcloud PropertySourceBootstrapConfiguration通过监听ApplicationContextInitializer<ConfigurableApplicationContext>在上下文初始化的时候把bootstrap配置文件加入到ApplicationContext，environment中。

```java
@Configuration
@EnableConfigurationProperties(PropertySourceBootstrapProperties.class)
public class PropertySourceBootstrapConfiguration implements
		ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

	public static final String BOOTSTRAP_PROPERTY_SOURCE_NAME = BootstrapApplicationListener.BOOTSTRAP_PROPERTY_SOURCE_NAME
			+ "Properties";

}
```



