###	事件

程序启动过程中，bean创建等过程中，程序结束过程中。springboot容器会发布一系列事件，让用户针对不同的事件、时机做相应的处理。提高应用程序的灵活性以及可扩展行。自己开发框架的时候要考虑。

事件是有顺序的，按照一定流程逐一发布

应用程序启动 -》准备环境 -》初始化上下文-》加载bean -》刷新上下文 -》 初始化web容器 -》程序可用响应web请求 -》 程序加载完成 

### springboot事件

1. ApplicationStartingEvent
2. ApplicationEnvironmentPreparedEvent
3. ApplicationContextInitializedEvent
4. ApplicationPreparedEvent
5. ContextRefreshedEvent
6. WebServerInitializedEvent
7. ServletWebServerInitializedEvent
8. ReactiveWebServerInitializedEvent
9. ApplicationStartedEvent
10. ApplicationReadyEvent
11. ApplicationFailedEvent



####	ApplicationStartingEvent

ApplicationStartingEvent在运行开始时发送，但在进行任何处理之前（侦听器和初始化程序的注册除外）发送。

####	ApplicationEnvironmentPreparedEvent

在上下文中使用的环境（Environment）但在创建上下文（context）之前，

####	ApplicationContextInitializedEvent

ApplicationContext已经准备并且任何bean没有被加载

####	ApplicationPreparedEvent

ApplicationContext以及加载完成但是没有刷新（refresh）bean definitions将会被加载并且环境（envionment）已经可用

####	ContextRefreshedEvent

initialized or refreshed 事件时触发

####	WebServerInitializedEvent

事件在刷新应用程序上下文并准备好WebServer后发布。用于获取正在运行的服务器的本地端口。

####	ApplicationStartedEvent

application context refresh之后，在任何CommandLineRunner 和 ApplicationRunner 之前

####	ApplicationReadyEvent

尽可能晚地发布事件，以表明应用程序已准备好为请求提供服务。

####	ApplicationFailedEvent

程序启动失败时发布



###	具体使用场景





