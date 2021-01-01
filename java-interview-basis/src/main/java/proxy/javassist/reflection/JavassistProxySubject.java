package proxy.javassist.reflection;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import proxy.Constant;
import proxy.RealSubject;

import java.text.DecimalFormat;


/**
 * @description:
 * @author: haochencheng
 * @create: 2019-07-25 18:39
 **/
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
