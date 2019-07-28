### proxy java代理模式
java动态代理实现方式主要有三种
1. cglib 
2. javassist
3. jdk


####    jdk proxy
jdk动态代理 基于接口，反射。
通过生成的代理类,代理类实现了被代理类的接口，有 invocationHandler 的引用以及被代理类的方法引用。
代理类调用 接口方法 -> 调用 invocationHandler.invoke方法 ，反射调用指定 被代理类方法。

Proxy.newProxyInstance 生成代理类 

```java

public class Proxy implements java.io.Serializable {

    /** parameter types of a proxy class constructor */
    private static final Class<?>[] constructorParams =
        { InvocationHandler.class };

    /**
     * a cache of proxy classes
     */
    private static final WeakCache<ClassLoader, Class<?>[], Class<?>>
        proxyClassCache = new WeakCache<>(new KeyFactory(), new ProxyClassFactory());

    /**
     * the invocation handler for this proxy instance.
     * @serial
     */
    protected InvocationHandler h;

    /**
    * 只有一个带参数 构造器
    * @param h
    */
    protected Proxy(InvocationHandler h) {
        Objects.requireNonNull(h);
        this.h = h;
    }
    
    /**
    * 生成代理类
    * @param loader
    * @param interfaces
    * @param h
    * @return 
    */
     public static Object newProxyInstance(ClassLoader loader,
                                              Class<?>[] interfaces,
                                              InvocationHandler h)
        {
            final Class<?>[] intfs = interfaces.clone();
    
            /*
             * 获取代理类 class  
             * Look up or generate the designated proxy class.
             */
            Class<?> cl = getProxyClass0(loader, intfs);
    
            /*
             * Invoke its constructor with the designated invocation handler.
             */
            final Constructor<?> cons = cl.getConstructor(constructorParams);
            final InvocationHandler ih = h;
            return cons.newInstance(new Object[]{h});
        }

    // proxyClassCache 中 有ProxyClassFactory 不存在key 通过 ProxyClassFactory 生成
    private static Class<?> getProxyClass0(ClassLoader loader,
                                           Class<?>... interfaces) {
        // If the proxy class defined by the given loader implementing
        // the given interfaces exists, this will simply return the cached copy;
        // otherwise, it will create the proxy class via the ProxyClassFactory
        return proxyClassCache.get(loader, interfaces);
    }
    
    
        /** 创建代理类 对象
         * A factory function that generates, defines and returns the proxy class given
         * the ClassLoader and array of interfaces.
         */
        private static final class ProxyClassFactory
            implements BiFunction<ClassLoader, Class<?>[], Class<?>>
        {
            // prefix for all proxy class names
            private static final String proxyClassNamePrefix = "$Proxy";
    
            // next number to use for generation of unique proxy class names
            private static final AtomicLong nextUniqueNumber = new AtomicLong();
    
            @Override
            public Class<?> apply(ClassLoader loader, Class<?>[] interfaces) {
    
                Map<Class<?>, Boolean> interfaceSet = new IdentityHashMap<>(interfaces.length);
                for (Class<?> intf : interfaces) {
                    /* 
                     * 做一些验证操作
                     * Verify that the class loader resolves the name of this
                     * interface to the same Class object.
                     */
                    Class<?>  interfaceClass = Class.forName(intf.getName(), false, loader);
                   
                }
    
                String proxyPkg = null;     // package to define proxy class in
                int accessFlags = Modifier.PUBLIC | Modifier.FINAL;
    
                /*
                 * 
                 * Record the package of a non-public proxy interface so that the
                 * proxy class will be defined in the same package.  Verify that
                 * all non-public proxy interfaces are in the same package.
                 */
                for (Class<?> intf : interfaces) {
                    int flags = intf.getModifiers();
                    if (!Modifier.isPublic(flags)) {
                        accessFlags = Modifier.FINAL;
                        String name = intf.getName();
                        int n = name.lastIndexOf('.');
                        String pkg = ((n == -1) ? "" : name.substring(0, n + 1));
                        if (proxyPkg == null) {
                            proxyPkg = pkg;
                        } else if (!pkg.equals(proxyPkg)) {
                            throw new IllegalArgumentException(
                                "non-public interfaces from different packages");
                        }
                    }
                }
    
                if (proxyPkg == null) {
                    // if no non-public proxy interfaces, use com.sun.proxy package
                    proxyPkg = ReflectUtil.PROXY_PACKAGE + ".";
                }
    
                /*
                 * Choose a name for the proxy class to generate.
                 */
                long num = nextUniqueNumber.getAndIncrement();
                String proxyName = proxyPkg + proxyClassNamePrefix + num;
    
                /*
                 * 通过 ProxyGenerator 生成 字节码
                 * Generate the specified proxy class.
                 */
                byte[] proxyClassFile = ProxyGenerator.generateProxyClass(
                    proxyName, interfaces, accessFlags);
                try {
                    return defineClass0(loader, proxyName,
                                        proxyClassFile, 0, proxyClassFile.length);
                } catch (ClassFormatError e) {
                    /*
                     * A ClassFormatError here means that (barring bugs in the
                     * proxy class generation code) there was some other
                     * invalid aspect of the arguments supplied to the proxy
                     * class creation (such as virtual machine limitations
                     * exceeded).
                     */
                    throw new IllegalArgumentException(e.toString());
                }
            }
        }
        
       
}

```
ProxyGenerator 生成java 字节码 
可以设置参数 输出生成的代理类 
 ```
    // 设置系统参数，输出动态生成的代理类
    System.setProperty("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");

```

####    cglib 动态代理
cglib 基于 java 继承和 MethodInterceptor 回调
生成的代理类中 有MethodInterceptor 引用，在MethodInterceptor.invoke中实现方法增强
继承被代理对象，通过重写父类方法，在其中调用Callback回调，在回调中进行方法增强
```java
public class RealSubject$$EnhancerByCGLIB$$380576fd extends RealSubject implements Factory {

    
     public final void speak() {
            MethodInterceptor var10000 = this.CGLIB$CALLBACK_0;
            if (var10000 == null) {
                CGLIB$BIND_CALLBACKS(this);
                var10000 = this.CGLIB$CALLBACK_0;
            }
    
            if (var10000 != null) {
                var10000.intercept(this, CGLIB$speak$1$Method, CGLIB$emptyArgs, CGLIB$speak$1$Proxy);
            } else {
                super.speak();
            }
        }
    
}
```