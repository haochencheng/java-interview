### java动态代理
具体代码参见： https://github.com/haochencheng/java-interview/tree/master/java-interview-basis/src/main/java/proxy

java动态代理实现方式主要有三种
1. cglib 
2. javassist
3. jdk

代理主题 Subject
```java
public interface Subject {

    void speak();

}
```
多个主题 DupSubject
```java
public interface DupSubject {

    void speakAgain();

}
```

被代理对象 RealSubject
```java
public class RealSubject implements Subject, DupSubject {


    @Override
    public void speakAgain() {
        System.out.println("DupSubject speakAgain");
    }

    @Override
    public void speak() {
//        System.out.println("Subject speak");
    }
}
```

下面看下具体的几种实现方式

####    jdk proxy
jdk动态代理 基于接口，反射。
通过生成的代理类,代理类实现了被代理类的接口，有 invocationHandler 的引用以及被代理类的方法引用。
代理类调用 接口方法 -> 调用 invocationHandler.invoke方法 ，反射调用指定 被代理类方法。
Proxy.newProxyInstance 生成代理类 

jdk动态代理实现代码
```java
package proxy.jdk;

import proxy.Constant;
import proxy.DupSubject;
import proxy.RealSubject;
import proxy.Subject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.DecimalFormat;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-07-25 14:35
 **/
public class JdkProxySubject implements InvocationHandler {

    private Object realSubject;

    /**
     *
     * 生成代理类
     *
     * @see  proxy.jdk.$Proxy1
     * @param realSubject
     * @return
     */
    public Object getProxySubject(Object realSubject) {
        this.realSubject = realSubject;
        return Proxy.newProxyInstance(realSubject.getClass().getClassLoader(), this.realSubject.getClass().getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //前置 增强处理
//        System.out.printf("before %s.%s \n",method.getDeclaringClass().getSimpleName(),method.getName());
        // jdk 1.7
        //method.setAccessible(true);
        // 不开 accessible jdk: 30952 ms, 45,556 t/s
        //                jdk: 3693 ms, 270,782 t/s
        //        jdk: 31446 ms, 44,840 t/s
        // 开了 jdk: 35308 ms, 39,936 t/s
        //     jdk: 3219 ms, 310,655 t/s
        //     jdk: 34773 ms, 40,550 t/s
        // 10w次 开了快 100w次 不开快 ？
        Object invoke = method.invoke(realSubject, args);
//        System.out.printf("after %s.%s \r\n",method.getDeclaringClass().getSimpleName(),method.getName());
        //后置 增强处理
        return invoke;
    }

    public static void main(String[] args) throws Exception {
        // 设置系统参数，输出动态生成的代理类
        System.setProperty("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");
        JdkProxySubject jdkProxySubject =new JdkProxySubject();
        //获取代理类
        Object proxySubjectTarget = jdkProxySubject.getProxySubject(new RealSubject());

        Subject subject =(Subject) proxySubjectTarget;
        //代理类调用指定 方法
        subject.speak();
        DupSubject dupSubject =(DupSubject) proxySubjectTarget;
        dupSubject.speakAgain();

    }

    static class Test {

        public static void main(String[] args) throws InterruptedException {
            test();
            return;
        }

    }

    private static void test() throws InterruptedException {
        int count = Constant.TH_W;
        Subject subject =(Subject) createJdkProxy();
        long time = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            subject.speak();
        }
        time = System.currentTimeMillis() - time;
        System.out.println("jdk-reflection-dynamic-proxy: " + time + " ms, " + new DecimalFormat().format(count * 1000 / time) + " t/s");
        Constant.debug();
    }

    public static Object createJdkProxy(){
        //获取代理类
        JdkProxySubject jdkProxySubject =new JdkProxySubject();
        Object proxySubjectTarget = jdkProxySubject.getProxySubject(new RealSubject());
        return proxySubjectTarget;
    }

}

```



看下Proxy源码
其中有一个InvocationHandler h;引用，用于回调自定义的InvocationHandler实现类
一个静态方法newProxyInstance(ClassLoader loader,Class<?>[] interfaces,InvocationHandler h)
参数为类加载器、被代理类实现的接口、自定义InvocationHandler实现类

```java
package java.lang.reflect;

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
             * 在proxyClassCache中获取 获取代理类 class  
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
通过查看ProxyGenerator 可以学习java .class 字节码的一些知识

在使用jdk动态代理的时候，可以设置参数 输出生成的代理类 
 ```
    // 设置系统参数，输出动态生成的代理类
    System.setProperty("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");

```

看下java生成的代理类，
代理类继承了Proxy接口（所以jdk动态代理只能基于接口代理，java单继承特性）
实现类被代理类的所有接口，持有InvocationHandler引用和被代理类的所有方法引用。
接口中通过InvocationHandler回调具体方法，实现方法增强。

```java
package proxy.jdk;

import proxy.DupSubject;
import proxy.RealSubject;
import proxy.Subject;

import java.lang.reflect.*;

/**
 * jdk生成的代理类
 * @see JdkProxySubject
 * @description:
 * @author: haochencheng
 * @create: 2019-07-25 15:34
 **/
public final class $Proxy1 extends Proxy implements Subject, DupSubject {
    private static Method m1;
    private static Method m2;
    private static Method m3;
    private static Method m4;
    private static Method m0;

    public $Proxy1(InvocationHandler var1) {
        super(var1);
    }

    @Override
    public final boolean equals(Object var1) {
        try {
            return (Boolean) super.h.invoke(this, m1, new Object[]{var1});
        } catch (RuntimeException | Error var3) {
            throw var3;
        } catch (Throwable var4) {
            throw new UndeclaredThrowableException(var4);
        }
    }

    @Override
    public final String toString() {
        try {
            return (String) super.h.invoke(this, m2, (Object[]) null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    @Override
    public final void speak() {
        try {
            super.h.invoke(this, m3, (Object[]) null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    @Override
    public final void speakAgain() {
        try {
            super.h.invoke(this, m4, (Object[]) null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    @Override
    public final int hashCode() {
        try {
            return (Integer) super.h.invoke(this, m0, (Object[]) null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    static {
        try {
            m1 = Class.forName("java.lang.Object").getMethod("equals", Class.forName("java.lang.Object"));
            m2 = Class.forName("java.lang.Object").getMethod("toString");
            m3 = Class.forName("proxy.Subject").getMethod("speak");
            m4 = Class.forName("proxy.DupSubject").getMethod("speakAgain");
            m0 = Class.forName("java.lang.Object").getMethod("hashCode");
        } catch (NoSuchMethodException var2) {
            throw new NoSuchMethodError(var2.getMessage());
        } catch (ClassNotFoundException var3) {
            throw new NoClassDefFoundError(var3.getMessage());
        }
    }

    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        Class<?> proxyClass = Proxy.getProxyClass($Proxy1.class.getClassLoader(), RealSubject.class.getInterfaces());
        Constructor<?>[] constructors = proxyClass.getConstructors();
        System.out.println(constructors);
    }

}

```


####    cglib 动态代理
cglib 基于 java 继承和 MethodInterceptor 回调
生成的代理类中 有MethodInterceptor 引用，在MethodInterceptor.invoke中实现方法增强
继承被代理对象，通过重写父类方法，在其中调用Callback回调，在回调中进行方法增强

cglib实现
```java

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-07-25 18:39
 **/
public class CjlibProxySubject implements MethodInterceptor {

    private Object target;

    // 持有被代理对象
    public CjlibProxySubject(Object target) {
        this.target = target;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        //前置 增强处理
//        System.out.printf("before %s.%s \n",method.getDeclaringClass().getSimpleName(),method.getName());
        // 直接调用被代理对象的方法
        Object invoke = method.invoke(target, objects);
        // 调用methodProxy的invokeSuper方法，注意如果调用methodProxy的invoke方法
        // 因为传入的obj为动态代理对象，则会陷入死循环，如果为被代理对象target，则不会
        // methodProxy.invokeSuper(o, objects);
//        System.out.printf("after %s.%s \r\n",method.getDeclaringClass().getSimpleName(),method.getName());
        //后置 增强处理
        return invoke;
    }

    public static void main(String[] args) {
        // 设置系统参数，输出动态生成的代理类
        String path = CjlibProxySubject.class.getResource("").getPath();
        System.out.printf("生成代理类路径：{%s}",path);
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, path);
        RealSubject realSubject = new RealSubject();
        CjlibProxySubject cjLibProxySubject = new CjlibProxySubject(realSubject);
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(realSubject.getClass());
        enhancer.setCallback(cjLibProxySubject);
        //代理对象
        RealSubject realSubject1 = (RealSubject) enhancer.create();
        realSubject1.speak();
        realSubject1.speakAgain();
    }

    public static Object createCjLibProxy(){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(RealSubject.class);
        enhancer.setCallback(new CjlibProxySubject(new RealSubject()));
        //代理对象
        return enhancer.create();
    }

    /**
     * 生成的代理类 其中一个方法 通过调用 方法拦截器 MethodInterceptor 方法增强（含有被代理类引用） -> 调用 被代理类方法
     * public final void speak() {
     *         MethodInterceptor var10000 = this.CGLIB$CALLBACK_0;
     *         if (var10000 == null) {
     *             CGLIB$BIND_CALLBACKS(this);
     *             var10000 = this.CGLIB$CALLBACK_0;
     *         }
     *
     *         if (var10000 != null) {
     *             var10000.intercept(this, CGLIB$speak$0$Method, CGLIB$emptyArgs, CGLIB$speak$0$Proxy);
     *         } else {
     *             super.speak();
     *         }
     *     }
     */
}
```

看下cglib生成的代理类
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
####    javassist
1. javassist 提供的动态代理工场，生成代理类，调用 MethodHandler ，使用java反射
性能比较低，主要是在反射上面。

2. 自己生成代理类 直接调用
性能高

####    javassist反射实现
```java
public class JavassistProxySubject {

    public static RealSubject createJavassistProxy() throws IllegalAccessException, InstantiationException {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(RealSubject.class);
        proxyFactory.setFilter(m -> {
            // ignore finalize()
            return !m.getName().equals("finalize");
        });
        Class c = proxyFactory.createClass();
        MethodHandler methodHandler = (self, thisMethod, proceed, args) -> proceed.invoke(self, args);
        Object proxy = c.newInstance();
        ((Proxy) proxy).setHandler(methodHandler);
        return (RealSubject) proxy;
    }

}
```

####    javassist-bytecode 实现
```java
public class JavassistBytecodeProxySubject {

    public static Object createProxy(RealSubject realSubject) throws Exception {
        ClassPool mPool = new ClassPool(true);
        CtClass proxy = mPool.makeClass(realSubject.getClass().getName() + "JavaassistProxy");
        proxy.setSuperclass(mPool.get(realSubject.getClass().getName()));
        proxy.addConstructor(CtNewConstructor.defaultConstructor(proxy));
        proxy.setModifiers(Modifier.PUBLIC);
        proxy.addMethod(CtNewMethod.make("public void speak() { return super.speak(); }", proxy));
        Class<?> pc = proxy.toClass();
        return pc.newInstance();
    }

}
```

####    性能测试

10w 

    [jdk-reflection-dynamic-proxy  9 ms, 11,111,111 t/s, 
    cglib-reflection-dynamic-proxy  15 ms, 6,666,666 t/s, 
    javassist-reflection-dynamic-proxy  14 ms, 7,142,857 t/s, 
    javassist-bytecode-static-proxy  2 ms, 50,000,000 t/s
    Process finished with exit code 0
    static-inherit-static-proxy  8 ms, 12,500,000 t/s, 
    static-interface-static-proxy  2 ms, 50,000,000 t/s]

1000w

    [jdk-reflection-dynamic-proxy  45 ms, 31,334,786 t/s, 
    cglib-reflection-dynamic-proxy  147 ms, 9,592,281 t/s,
    javassist-reflection-dynamic-proxy  149 ms, 9,463,526 t/s,
    javassist-bytecode-reflection-dynamic-proxy  43 ms, 32,792,218 t/s
    static-inherit-static-proxy  45 ms, 31,334,786 t/s, 
    static-interface-static-proxy  65 ms, 21,693,313 t/s]
    
可见jdk8 jdk动态代理、javassist-bytecode 性能和直接调用差不多，
jdk8 优化了动态代理 ，反射调用超过15次后，使用MethodAccessorGenerator 生成代理类 字节码很少，接近 直接调用。    

