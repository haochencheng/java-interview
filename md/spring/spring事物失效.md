###	事物传播行为

Spring的传播行为

```java
 public enum Propagation {
 	 REQUIRED(0),
   SUPPORTS(1),
   MANDATORY(2),
   REQUIRES_NEW(3),
   NOT_SUPPORTED(4),
   NEVER(5),
   NESTED(6); 
 } 
```



- REQUIRED ：

  如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务。 

- SUPPORTS ：

  如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行。 

- MANDATORY ：

  如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常。 

- REQUIRES_NEW ：

  创建一个新的事务，如果当前存在事务，则把当前事务挂起。 

- NOT_SUPPORTED ：

  以非事务方式运行，如果当前存在事务，则把当前事务挂起。 

- NEVER ：

  以非事务方式运行，如果当前存在事务，则抛出异常。 

- NESTED ：

  如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；如果当前没有事务，则该取值等价于 REQUIRED 。

###	事物失效

spirng事物通过spirngAOP使用动态代理（jdk动态代理或者cglib动态代理）的方式实现事物管理，在运行时织入，在代理类中做开启事物，提交事物，回滚事物操作。

失效原因：调用类本身，没有调用代理类。

```java
@Service
@Slf4j
public class UserService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserService2 userService2;

    @Transactional
    public void save() {
        jdbcTemplate.execute("INSERT INTO user (id, name) VALUES\n" +
                "(5, 'Jack5')");
        try {
            save2();
        } catch (Exception e) {
            System.err.println("出错啦");
        }

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save2() {
        jdbcTemplate.execute("INSERT INTO user (id, name) VALUES\n" +
                "(6, 'Jack6')");
        int i = 1 / 0;
    }
}
```

未加注解 @Transactional自调用失效

```java

    public void save() {
        save2();
    }

    @Transactional()
    public void save2() {
        jdbcTemplate.execute("INSERT INTO user (id, name) VALUES\n" +
                "(7, 'Jack7')");
        int i = 1 / 0;
    }
```

私有方法加@Transactional无效

spring使用Aop时管理事物时，不会拦截private方法

```java
    @Transactional()
    private void save2() {
        jdbcTemplate.execute("INSERT INTO user (id, name) VALUES\n" +
                "(7, 'Jack7')");
        int i = 1 / 0;
    }
```

#### AbstractFallbackTransactionAttributeSource

```
private TransactionAttribute computeTransactionAttribute(Method method, Class<?> targetClass) {
			// Don't allow no-public methods as required.
			if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {
				return null;
			}
			...
}
```

这里需要对 Spring 生成的代理对象来管理造成一个自调用问题。解释一下：

在应用系统采用了 -> 调用声明@Transactional 的目标方法时，Spring Framework 默认使用 AOP 代理，在代码运行时生成一个代理对象，根据@Transactional 的属性配置信息，这个代理对象决定该声明@Transactional 的目标方法是否由拦截器 TransactionInterceptor 来使用拦截，在 TransactionInterceptor 拦截时，会在目标方法开始执行之前创建并加入事务，并执行目标方法的逻辑;分析这句话 意思也就是要想让spring的声明式@Transactional 生效，必须要经过 拦截器 TransactionInterceptor处理，而自调用问题导致的原因也正是TransactionInterceptor没有 拦截到。

解决方法：

根本原因是 由于使用 Spring AOP 代理造成的。所以替代Spring AOP 代理问题就解决了，

 使用 AspectJ 取代 Spring AOP 代理。