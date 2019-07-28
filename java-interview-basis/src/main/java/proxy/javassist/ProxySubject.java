package proxy.javassist;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.springframework.cglib.proxy.Enhancer;
import proxy.Constant;
import proxy.RealSubject;

import java.text.DecimalFormat;


/**
 * @description:
 * @author: haochencheng
 * @create: 2019-07-25 18:39
 **/
public class ProxySubject {

    public static Object getProxy(Class<?> cl) throws IllegalAccessException, InstantiationException {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(cl);
        proxyFactory.setFilter(m -> {
            // ignore finalize()
            return !m.getName().equals("finalize");
        });
        Class c = proxyFactory.createClass();
        MethodHandler methodHandler = (self, thisMethod, proceed, args) -> {
            System.out.println("method name: " + thisMethod.getName() + " exec");
            return proceed.invoke(self, args);
        };
        Object proxy = c.newInstance();
        ((Proxy) proxy).setHandler(methodHandler);
        return proxy;
    }

    static class Test {

        public static void main(String[] args) throws InstantiationException, IllegalAccessException, InterruptedException {
            int count = Constant.W;
            RealSubject realSubject =(RealSubject) getProxy(RealSubject.class);
            long time = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                realSubject.speak();
            }
            time = System.currentTimeMillis() - time;
            System.out.println("javassist: " + time + " ms, " + new DecimalFormat().format(count * 1000 / time) + " t/s");
            Constant.debug();
        }

    }



}
