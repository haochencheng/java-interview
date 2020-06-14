###	springboot启动流程

Springboot/spring 启动几个阶段

1.准备环境

2.加载解析配置文件

3.加载bean

4.初始化web容器

5.启动完成

springboot启动程序栈信息

![启动](https://raw.githubusercontent.com/haochencheng/java-interview/master/md/spingboot/pic/springboot-start.png)

###	SpringApplication

执行springApplication，创建并且刷新ApplicationContext，返回ConfigurableApplicationContext（可配置的上下文）。

```java
public class SpringApplication {

/**
	 * Run the Spring application, creating and refreshing a new
	 * {@link ApplicationContext}.
	 * @param args the application arguments (usually passed from a Java main method)
	 * @return a running {@link ApplicationContext}
	 */
	public ConfigurableApplicationContext run(String... args) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ConfigurableApplicationContext context = null;
		Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
		configureHeadlessProperty();
		SpringApplicationRunListeners listeners = getRunListeners(args);
		listeners.starting();
		try {
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(
					args);
			ConfigurableEnvironment environment = prepareEnvironment(listeners,
					applicationArguments);
			configureIgnoreBeanInfo(environment);
			Banner printedBanner = printBanner(environment);
			context = createApplicationContext();
			exceptionReporters = getSpringFactoriesInstances(
					SpringBootExceptionReporter.class,
					new Class[] { ConfigurableApplicationContext.class }, context);
			prepareContext(context, environment, listeners, applicationArguments,
					printedBanner);
			refreshContext(context);
			afterRefresh(context, applicationArguments);
			stopWatch.stop();
			if (this.logStartupInfo) {
				new StartupInfoLogger(this.mainApplicationClass)
						.logStarted(getApplicationLog(), stopWatch);
			}
			listeners.started(context);
			callRunners(context, applicationArguments);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, exceptionReporters, listeners);
			throw new IllegalStateException(ex);
		}

		try {
			listeners.running(context);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, exceptionReporters, null);
			throw new IllegalStateException(ex);
		}
		return context;
	}
}
```



1. 准备环境

```java
ConfigurableEnvironment environment = prepareEnvironment(listeners,
					applicationArguments);
```



2. 创建上下文

```
	context = createApplicationContext();
```

3. 准备上下文

```
prepareContext(context, environment, listeners, applicationArguments,
					printedBanner);
```

4. 刷新上下文

```
	refreshContext(context);
```

5. 刷新后操作

```
afterRefresh(context, applicationArguments);
```

6. 开启监听

```
listeners.started(context);
```

7. 执行启动后初始化类

```
	callRunners(context, applicationArguments);
```

8. 运行监听

```
	listeners.running(context);
```



