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
public class ProxySubject implements InvocationHandler {

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
        ProxySubject proxySubject=new ProxySubject();
        //获取代理类
        Object proxySubjectTarget = proxySubject.getProxySubject(new RealSubject());

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
        ProxySubject proxySubject=new ProxySubject();
        Object proxySubjectTarget = proxySubject.getProxySubject(new RealSubject());
        return proxySubjectTarget;
    }


    static class Gen {
    }



}
