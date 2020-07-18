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

在spring启动调用refresh()中的
```
// Invoke factory processors registered as beans in the context.
invokeBeanFactoryPostProcessors(beanFactory);
```
方法实例化所有注册的BeanFactoryPostProcessor
```
public void processConfigBeanDefinitions(BeanDefinitionRegistry registry) {processConfigBeanDefinitions(BeanDefinitionRegistry registry) 
```
...
。。。
此方法中会调用
```
ConfigurationClassPostProcessor.processConfigBeanDefinitions
处理配置文件bean定义
1.首先会对配置文件排序
2.处理@Configuration配置		// Parse each @Configuration class
	2.1 判断是注解bean还是普通bean
	2.2 注解bean通过 ConfigurationClassParser 处理
		2.2.1 调用ConfigurationClassParser.doProcessConfigurationClass方法
		2.2.1.1  处理@PropertySource	// Process any @PropertySource annotations
		2.2.1.2  处理@ComponentScan // Process any @ComponentScan annotations
		2.2.1.3  处理@Import 	// Process any @Import annotations
		2.2.1.4  处理@ImportResource  	// Process any @ImportResource annotations
		2.2.1.5  处理@Bean  // Process individual @Bean methods
		2.2.1.6 // Process default methods on interfaces
		2.2.1.7 // Process superclass, if any
	2.3 调用 this.deferredImportSelectorHandler.process();	 
	// deferredImportSelector  最后调用 先处理 注解类中其他注解，然后 实例化该注解类 ，
	// 执行 selectImports在 ImportSelector 后 
```
其中  2.2.1.3  处理@Import 分三种情况处理
1. ImportSelector.class 处理导入handler，返回需要实例化的bean name[]
2. ImportBeanDefinitionRegistrar.class 通过注解导入bean定义，自己实现
3. 	// Candidate class not an ImportSelector or ImportBeanDefinitionRegistrar ->
	// process it as an @Configuration class
	按照普通@Configuration 处理
解析默认 忽略 java.lang.annotation.  和 org.springframework.stereotype. 注解

AutoConfigurationImportSelector 类实现接口 DeferredImportSelector
