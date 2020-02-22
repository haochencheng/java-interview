###	Eureka

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

```java

```

