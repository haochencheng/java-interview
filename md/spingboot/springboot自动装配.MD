###	自动装配

自动把配置类注册到spring容器中

1.EnableAutoConfiguration 自动注册 ，加载spring.factories配置文件，将里面配置的类注入

2.手动通过注解@Enabled*注解启用

####	@SpringBootApplication

组合注解

- @EnableAutoConfiguration

```java
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = {
		@Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {
}
```

####	EnableAutoConfiguration

```
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {

}
```

EnableAutoConfiguration是常规的spirng@Configuration

###	SpringFactoriesLoader

通过SpringFactoriesLoader加载META-INF/spring.factories配置文件。

EnableAutoConfiguration 通过@Import注解导入自动装配选择AutoConfigurationImportSelector

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration,\
```



####	AutoConfigurationImportSelector

AutoConfigurationImportSelector解析SpringFactoriesLoader加载的资源，过滤，排重返回类全路径数组String[]

```java
public class AutoConfigurationImportSelector
		implements DeferredImportSelector, BeanClassLoaderAware, ResourceLoaderAware,
		BeanFactoryAware, EnvironmentAware, Ordered {

@Override
	public String[] selectImports(AnnotationMetadata annotationMetadata) {
		if (!isEnabled(annotationMetadata)) {
			return NO_IMPORTS;
		}
		AutoConfigurationMetadata autoConfigurationMetadata = AutoConfigurationMetadataLoader
				.loadMetadata(this.beanClassLoader);
		AnnotationAttributes attributes = getAttributes(annotationMetadata);
		List<String> configurations = getCandidateConfigurations(annotationMetadata,
				attributes);
		configurations = removeDuplicates(configurations);
		Set<String> exclusions = getExclusions(annotationMetadata, attributes);
		checkExcludedClasses(configurations, exclusions);
		configurations.removeAll(exclusions);
		configurations = filter(configurations, autoConfigurationMetadata);
		fireAutoConfigurationImportEvents(configurations, exclusions);
		
		}
}		

```

Spring Boot检查发布的jar中是否存在META-INF / spring.factories文件。该文件应在EnableAutoConfiguration键下列出您的配置类（自动装配的类）

- 先关注解

  ```
   @AutoConfigureAfter
   @AutoConfigureBefore
   @AutoConfigureOrder.
  ```



###	创建自己的starter

一个完整的starter应该包含以下两个组件

- The `autoconfigure` module that contains the auto-configuration code.

- 一个starter提供依赖



####	手动启用@Enabled注解

```java
@Import({TransactionManagementConfigurationSelector.class})
public @interface EnableTransactionManagement {
}
```



Enabled注解使用@Import注解导入实现

1.ImportSelector

2.DeferredImportSelector

3.ImportBeanDefinitionRegistrar

接口的类，该类中方法返回类会被spring管理String[] 