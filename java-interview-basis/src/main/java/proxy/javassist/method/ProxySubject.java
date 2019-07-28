package proxy.javassist.method;


import javassist.*;


/**
 * @description:
 * @author: haochencheng
 * @create: 2019-07-28 12:21
 **/
public class ProxySubject {

    public static void main(String[] args) {

    }

    public static Object getProxy(Class<?> cl) throws IllegalAccessException, InstantiationException {
        ClassPool mPool = new ClassPool(true);
        CtClass mCtc = mPool.makeClass(cl.getClass().getName() + "JavaassistProxy");
        return null;
    }

}
