package proxy.jdk;

import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.cglib.proxy.Enhancer;
import proxy.DupSubject;
import proxy.RealSubject;
import proxy.Subject;

import java.io.IOException;
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

        public static void main(String[] args) {
            int count = 10000000;
            Subject subject =(Subject) createJdkProxy();
            long time = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                subject.speak();
            }
            time = System.currentTimeMillis() - time;
            System.out.println("jdk: " + time + " ms, " + new DecimalFormat().format(count * 1000 / time) + " t/s");
        }

        private static Object createJdkProxy(){
            //获取代理类
            ProxySubject proxySubject=new ProxySubject();
            Object proxySubjectTarget = proxySubject.getProxySubject(new RealSubject());
            return proxySubjectTarget;
        }
    }




}
