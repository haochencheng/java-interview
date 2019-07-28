package proxy.javassist.staticproxy;


import javassist.*;
import proxy.RealSubject;


/**
 * javassist动态代理通过继承方式
 * @description:
 * @author: haochencheng
 * @create: 2019-07-28 12:21
 **/
public class JavassistBytecodeProxySubject {

    public static void main(String[] args) throws Exception {
        Object proxy = createProxy(new RealSubject());
        ((RealSubject)proxy).speak();
    }

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
