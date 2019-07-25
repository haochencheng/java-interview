# java-interview-tomcat
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