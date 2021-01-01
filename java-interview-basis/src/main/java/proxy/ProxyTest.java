package proxy;

import proxy.cglib.CjlibProxySubject;
import proxy.javassist.reflection.JavassistProxySubject;
import proxy.javassist.staticproxy.JavassistBytecodeProxySubject;
import proxy.jdk.JdkProxySubject;
import proxy.staticproxy.StaticInheritProxySubject;
import proxy.staticproxy.StaticInterfaceProxySubject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-07-28 13:07
 **/
public class ProxyTest {

    private static List<String> arrayList=new ArrayList<>();

    public static void main(String[] args) throws Exception {
        int count=Constant.TH_W;
        testJdkDynamicProxy(count);
        testCglibDynamicProxy(count);
        testJavassistDynamicProxy(count);
        testStaticInheritProxySubject(count);
        testStaticInterfaceProxySubject(count);
        testJavassistBytecodeProxy(count);
        System.out.println(Arrays.toString(arrayList.toArray()));
    }

    private static void testJdkDynamicProxy(int count) {
        test("jdk" + Constant.DYNAMIC_PROXY, count, JdkProxySubject.createJdkProxy());
    }

    private static void testCglibDynamicProxy(int count) {
        test("cglib" + Constant.DYNAMIC_PROXY, count, CjlibProxySubject.createCjLibProxy());
    }

    private static void testJavassistDynamicProxy(int count) throws InstantiationException, IllegalAccessException {
        test("javassist" + Constant.DYNAMIC_PROXY, count, JavassistProxySubject.createJavassistProxy());
    }

    private static void testJavassistBytecodeProxy(int count) throws Exception {
        test("javassist-bytecode" + Constant.STATIC_PROXY, count, JavassistBytecodeProxySubject.createProxy(new RealSubject()));
    }

    private static void testStaticInheritProxySubject(int count)  {
        test("static-inherit" + Constant.STATIC_PROXY, count, new StaticInheritProxySubject());
    }

    private static void testStaticInterfaceProxySubject(int count)  {
        test("static-interface" + Constant.STATIC_PROXY, count, new StaticInterfaceProxySubject(new RealSubject()));
    }

    private static void test(String type, int count, Object subject) {
        long time = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            ((Subject) subject).speak();
        }
        time = System.currentTimeMillis() - time;
        String printStr=type+"  " + time + " ms, " + new DecimalFormat().format(count * 1000 / time) + " t/s";
        arrayList.add(printStr);
    }


}
