package proxy.cglib;

import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import proxy.RealSubject;

import java.lang.reflect.Method;
import java.text.DecimalFormat;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-07-25 18:39
 **/
public class ProxySubject implements MethodInterceptor {

    private Object target;

    // 持有被代理对象
    public ProxySubject(Object target) {
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
        String path = ProxySubject.class.getResource("").getPath();
        System.out.printf("生成代理类路径：{%s}",path);
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, path);
        RealSubject realSubject = new RealSubject();
        ProxySubject proxySubject = new ProxySubject(realSubject);
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(realSubject.getClass());
        enhancer.setCallback(proxySubject);
        //代理对象
        RealSubject realSubject1 = (RealSubject) enhancer.create();
        realSubject1.speak();
        realSubject1.speakAgain();
    }

    static class Test {

        public static void main(String[] args) {
            int count = 10000000;
            RealSubject realSubject =(RealSubject) createCjLibProxy();
            long time = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                realSubject.speak();
            }
            time = System.currentTimeMillis() - time;
            System.out.println("cglib: " + time + " ms, " + new DecimalFormat().format(count * 1000 / time) + " t/s");
        }

        private static Object createCjLibProxy(){
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(RealSubject.class);
            enhancer.setCallback(new ProxySubject(new RealSubject()));
            //代理对象
            return enhancer.create();
        }
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
