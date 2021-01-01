###	外部化配置（Externalized Configuration）

springboot可以使用外部化配置，在不同的环境分使用相同的代码。

你可以使用配置文件.properties、YAML文件、可用的环境、命令行参数。

配置文件key使用@Value注入bean

可以通过`@ConfigurationProperties`.绑定到结构化数据

Spring Boot使用一个非常特殊的PropertySource顺序，该顺序旨在允许合理地覆盖值。按以下顺序考虑属性：

一、全局配置

- 当devtools被激活时， $HOME/.config/spring-boot 文件夹下的任何文件

全局设置

1. `spring-boot-devtools.properties`
2. `spring-boot-devtools.yaml`
3. `spring-boot-devtools.yml`

二、测试配置

- [`@TestPropertySource`](https://docs.spring.io/spring/docs/5.2.3.RELEASE/javadoc-api/org/springframework/test/context/TestPropertySource.html) 

  annotations on your tests

- @SpringBootTest

三、命令行配置 环境变量

- Command line arguments.

- Properties from `SPRING_APPLICATION_JSON`

inline JSON embedded in an environment variable or system property

eg。  

```sh
java -Dspring.application.json='{"name":"test"}' -jar myapp.jar
```

```sh
SPRING_APPLICATION_JSON='{"acme":{"name":"test"}}' java -jar myapp.jar
```

```sh
java -jar myapp.jar --spring.application.json='{"name":"test"}'
```

四、servlet配置

- `ServletConfig` init parameters.
- `ServletContext` init parameters.

五、java配置

- JNDI attributes from `java:comp/env`.
- java System properties (`System.getProperties()`).

- OS environment variables.

六、profile配置

- Profile-specific application properties outside of your packaged jar (`application-{profile}.properties` and YAML variants).

  jar包外的特殊的profile配置

- Profile-specific application properties packaged inside your jar (`application-{profile}.properties` and YAML variants).

  jar包内的特殊profile配置

- Application properties outside of your packaged jar (`application.properties` and YAML variants).

  jar包外的Application配置

- Application properties packaged inside your jar (`application.properties` and YAML variants).

  jar包内的Application配置

七、程序内的配置（代码配置）

- @PropertySource annotations on your `@Configuration` classes. 

注意这样的变量在程序refreshed前不会加载到Environment中

- Default properties (specified by setting `SpringApplication.setDefaultProperties`).

  默认的配置文件

