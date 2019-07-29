# java-interview-tomcat
tomcat笔记

####    tomcat中组件

![java类加载器](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/tomcat/tomcat-design.png)

一个Server包含多个Service，一个Service维护多个Connector和Engine，一个Engine包含多个Host（虚拟主机），一个Host包含多个Context（应用），
一个Context包含多个Wrapper（servlet定义）


####    Pipeline （管道）和 Value（阀）
在增强组件的灵活性和可拓展性方面，责任链模式是一种比较好的选择（例如：filter）。
tomcat采用责任链模式处理请求，每个Container组件通过一个责任链完成具体请求。

tomcat定义了 Pipeline 和 Value两个接口。前者用于构造责任链，后者代表责任链上的每个处理器.
Pipeline中 关联了一个 Container， 定义了Valve[]责任链集合，最后处理请求的Value basic并且定义了相关接口

获取 所有Value[]的接口 和 添加Value 设置basic 获取basic的接口
Value是一个单向链表
```java
public interface Pipeline {
    public Valve getBasic();
    public void setBasic(Valve valve);
    public void addValve(Valve valve);
    public Valve[] getValves();
}
```
Value是一个单向链表
```java
public interface Valve {
    
    //单向链表 获取下一个Value
    public Valve getNext();

    public void setNext(Valve valve);

    /**
     * 执行周期性定时任务 
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     */
    public void backgroundProcess();

    //处理请求 责任链中逐一调用 最后调用basic
    public void invoke(Request request, Response response)
        throws IOException, ServletException;

    
    public boolean isAsyncSupported();
}

```
Pipeline有一个实现类StandardPipeline

```java
public class StandardPipeline extends LifecycleBase
        implements Pipeline, Contained {
    
     /**
         * The basic Valve (if any) associated with this Pipeline.
         */
        protected Valve basic = null;
    
    
        /**
         * The Container with which this Pipeline is associated.
         */
        protected Container container = null;
    
    
        /**
         * The first valve associated with this Pipeline.
         */
        protected Valve first = null;

        
        /**
        * 添加Valve 到 basic 前
        * @param valve
        */
        @Override
        public void addValve(Valve valve) {
    
            // Validate that we can add this Valve
            if (valve instanceof Contained)
                ((Contained) valve).setContainer(this.container);
    
            // Start the new component if necessary
            if (getState().isAvailable()) {
                if (valve instanceof Lifecycle) {
                    try {
                        ((Lifecycle) valve).start();
                    } catch (LifecycleException e) {
                        log.error("StandardPipeline.addValve: start: ", e);
                    }
                }
            }
    
            // Add this Valve to the set associated with this Pipeline
            if (first == null) {
                first = valve;
                valve.setNext(basic);
            } else {
                // 
                Valve current = first;
                while (current != null) {
                    if (current.getNext() == basic) {
                        current.setNext(valve);
                        valve.setNext(basic);
                        break;
                    }
                    current = current.getNext();
                }
            }
    
            container.fireContainerEvent(Container.ADD_VALVE_EVENT, valve);
        } 
       
}
```

### tomcat 中组件说明
            
| 组件        | 说明    | 
| --------   | -----:   |
| Server        | 表示整个servlet容器，因此tomcat运行环境中只有唯一一个Server实例       |   
| Service        | Service表示一个或多个Connector集合，这些Connector共享一个Container来处理其请求，
在同一个Tomcat中可以包含任意多个Service，他们彼此独立      |   
| Connector        | Tomcat链接器 ，用于监听并转化Socket请求，同时将读取的socket请求交于Container处理，支持不同的协议以及I/O方式     |   
| Container |  Container 表示能够执行客户端请求并返回响应的一类对象 ，在tomcat中存在不同级别的容器,Engine、Host、Context、Wrapper |
| Engine | Engine表示整个servlet引擎，在Tomcat中，Engine为最高层级的容器对象，尽管Engine不是直接处理请求的容器，但确是获取目标容器的入口 |
| Host | Host作为一类容器，表示Servlet引擎（Engine）中的虚拟机，与一个服务器的网络名有关，如域名等，客户端可以使用这个网络名连接服务器，这个名称必须在DNS服务器注册 |
| Context | Context作为一类容器，表示ServletContext，在servlet规范中，一个ServletContext表示一个独立的web应用 |
| Wrapper | Wrapper作为一类容器，用于表示Web应用中定义的servlet |
| Executor | 表示Tomcat组件可共享的线程池 |



### java类加载器
![java类加载器](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/tomcat/java-classloader.png)

java类加载器 Bootstrap ClassLoader -> Extension Classloader -> System Classloader
 ```java
public abstract class ClassLoader {

    protected Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException
        {
            //同步锁
            synchronized (getClassLoadingLock(name)) {
                // 首先检查类是否已经被加载 如果已经被加载则返回
                // First, check if the class has already been loaded
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    long t0 = System.nanoTime();
                    try {
                        // 如果父类加载器不为null 委托父类加载
                        if (parent != null) {
                            c = parent.loadClass(name, false);
                        } else {
                            //如果没有父类加载器 查找Bootstrap加载器是否加载过此类
                            c = findBootstrapClassOrNull(name);
                        }
                    } catch (ClassNotFoundException e) {
                        // ClassNotFoundException thrown if class not found
                        // from the non-null parent class loader
                    }
    
                    // 如果还是没有加载过，调用 子类加载器加载
                    if (c == null) {
                        // If still not found, then invoke findClass in order
                        // to find the class.
                        long t1 = System.nanoTime();
                        c = findClass(name);
    
                        // this is the defining class loader; record the stats
                        sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                        sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                        sun.misc.PerfCounter.getFindClasses().increment();
                    }
                }
                // 是否链接 
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        }
        
        
        protected final Class<?> findLoadedClass(String name) {
           if (!checkName(name))
               return null;
           return findLoadedClass0(name);
       } 
       
        /**
        * 链接类，如果类已链接，直接返回。否则链接类
        * 类的加载生命周期。 .java -> 经过javac（java编译器）编译成字节码 -> 类加载器加载
        * -> 加载、链接（验证、准备、解析）、初始化 -> 使用 -> 卸载
        * Links the specified class.  This (misleadingly named) method may be
        * used by a class loader to link a class.  If the class <tt>c</tt> has
        * already been linked, then this method simply returns. Otherwise, the
        * class is linked as described in the "Execution" chapter of
        * <cite>The Java&trade; Language Specification</cite>.
        *
        * @param  c
        *         The class to link
        *
        * @throws  NullPointerException
        *          If <tt>c</tt> is <tt>null</tt>.
        *
        * @see  #defineClass(String, byte[], int, int)
        */
       protected final void resolveClass(Class<?> c) {
           resolveClass0(c);
       }

}

```

类的生命周期
1. 加载
2. 链接 
3. 初始化

####    加载 
类加载器加载.class字节码

####    链接
一般会跟加载阶段和初始化阶段交叉进行
- 验证：
    当一个类被加载之后，必须要验证一下这个类是否合法，比如这个类是不是符合字节码的格式、
    变量与方法是不是有重复、数据类型是不是有效、继承与实现是否合乎标准等等。
    总之，这个阶段的目的就是保证加载的类是能够被jvm所运行。
- 准备：
    准备阶段的工作就是为类的静态变量分配内存并设为jvm默认的初值，对于非静态的变量，
    则不会为它们分配内存。有一点需要注意，这时候，静态变量的初值为jvm默认的初值，而不是我们在程序中设定的初值。
    jvm默认的初值是这样的：
    基本类型（int、long、short、char、byte、boolean、float、double）的默认值为0。
    引用类型的默认值为null。
    常量的默认值为我们程序中设定的值，比如我们在程序中定义final static int a = 100，
    则准备阶段中a的初值就是100。
- 解析：
    这一阶段的任务就是把常量池中的符号引用转换为直接引用（内存地址）。

####    初始化
如果一个类被直接引用，就会触发类的初始化。 初始化父类-> 父类静态代码块 -> 父类静态变量 -> 初始化子类 -> 子类静态代码块 -> 子类静态变量
- 通过new关键字实例化对象、读取或设置类的静态变量、调用类的静态方法。
- 通过反射方式执行以上三种行为。
- 初始化子类的时候，会触发父类的初始化。
- 作为程序入口直接运行时（也就是直接调用main方法）。

####    卸载
　由Java虚拟机自带的类加载器所加载的类，在虚拟机的生命周期中，始终不会被卸载。
1. 该类所有的实例都已经被回收，也就是java堆中不存在该类的任何实例。
2. 加载该类的ClassLoader已经被回收。
3. 该类对应的java.lang.Class对象没有任何地方被引用，无法在任何地方通过反射访问该类的方法。

    
    如果以上三个条件全部满足，jvm就会在方法区垃圾回收的时候对类进行卸载，类的卸载过程其实就是在方法区中清空类信息，java类的整个生命周期就结束了。

### tomcat 类加载器
servlet规范要求每个web应用程序都有一个类加载器

1. 隔离性 
    
    
    不同应用程序依赖库隔离
    
2. 灵活性 


    重新部署一个应用不会影响其他应用
    

3. 性能
    
    
    每个应用程序都有一个类加载器，不用搜索其他应用程序jar包    


![tomcat-classLoader](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/tomcat/tomcat-classloader.png)

tomcat提供了三个基础的类加载器和web应用类加载器
- Common
    以System为父的类加载器，位于tomcat应用服务器顶层的公用加载器。
器路径为common.loader,默认只想$CATALINA_HOME/lib下的包。

- Catalina
    以Common为父加载器，是用于加载Tomcat应用服务器的类加载器
其路径为server.loader,默认为空。此时tomcat使用Common类加载器

- Shared
    以Common为父加载器，是所有web Application的父加载器，
    其路径为shared.loader.默认为空，此时使用Common类加载器

- web应用

    以shared为父加载器，加载/WEB-INF/class 下的未压缩class和资源文件
    以及/WEB-INF/lib 目录下的jar包，只对当前应用可见，对其他应用不可见。
    

### Catalina
tomcat分层图
![tomcat-layer](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/tomcat/tomcat-layer.png)


#### 创建Server
 Catalina 使用 digester 解析Server.xml创建Server。
 
### web应用加载
Catalina对web应用的加载主要由StandardHost、HostConfig、StandardContext、Context-config、StandardWrapper
这5个类完成

TODO ...

### Web 请求处理
tomcat通过org.apache.catalina.mapper.Mapper维护请求连接与Host、Context、Wrapper等Container的映射。
MapperListener监听所有的


### coyote
Coyote是tomcat链接器框架的名称，是tomcat服务器提供的供客户端访问的外部接口。
客户端通过Coyote与服务器建立链接，发送请求并接收响应。

![tomcat-coyote](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/tomcat/tomcat-coyote.png)

在Coyote中tomcat支持三种协议，
1. HTTP/1.1 
2. HTTP/2.0 (tomcat8.5以后版本开始支持)
3. AJP协议

针对HTTP协议和AJP协议，tomcat又按照不同的I/O方式分别通过了不同的选择方案
tomcat8.5版本开始溢出了对BIO的支持

NIO：采用java NIO类库实现
NOI2：采用JDK7最新NOI2库实现
APR：采用apache APR实现。是C++编写的本地库，如果使用需本地安装 APR

![tomcat-transport](https://raw.githubusercontent.com/haochencheng/java-interview/master/pic/tomcat/tomcat-transport.png)



