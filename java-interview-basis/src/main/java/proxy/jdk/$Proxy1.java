package proxy.jdk;

import proxy.DupSubject;
import proxy.RealSubject;
import proxy.Subject;

import java.lang.reflect.*;

/**
 * jdk生成的代理类
 * @see JdkProxySubject
 * @description:
 * @author: haochencheng
 * @create: 2019-07-25 15:34
 **/
public final class $Proxy1 extends Proxy implements Subject, DupSubject {
    private static Method m1;
    private static Method m2;
    private static Method m3;
    private static Method m4;
    private static Method m0;

    public $Proxy1(InvocationHandler var1) {
        super(var1);
    }

    @Override
    public final boolean equals(Object var1) {
        try {
            return (Boolean) super.h.invoke(this, m1, new Object[]{var1});
        } catch (RuntimeException | Error var3) {
            throw var3;
        } catch (Throwable var4) {
            throw new UndeclaredThrowableException(var4);
        }
    }

    @Override
    public final String toString() {
        try {
            return (String) super.h.invoke(this, m2, (Object[]) null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    @Override
    public final void speak() {
        try {
            super.h.invoke(this, m3, (Object[]) null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    @Override
    public final void speakAgain() {
        try {
            super.h.invoke(this, m4, (Object[]) null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    @Override
    public final int hashCode() {
        try {
            return (Integer) super.h.invoke(this, m0, (Object[]) null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    static {
        try {
            m1 = Class.forName("java.lang.Object").getMethod("equals", Class.forName("java.lang.Object"));
            m2 = Class.forName("java.lang.Object").getMethod("toString");
            m3 = Class.forName("proxy.Subject").getMethod("speak");
            m4 = Class.forName("proxy.DupSubject").getMethod("speakAgain");
            m0 = Class.forName("java.lang.Object").getMethod("hashCode");
        } catch (NoSuchMethodException var2) {
            throw new NoSuchMethodError(var2.getMessage());
        } catch (ClassNotFoundException var3) {
            throw new NoClassDefFoundError(var3.getMessage());
        }
    }

    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        Class<?> proxyClass = Proxy.getProxyClass($Proxy1.class.getClassLoader(), RealSubject.class.getInterfaces());
        Constructor<?>[] constructors = proxyClass.getConstructors();
        System.out.println(constructors);
    }

}
