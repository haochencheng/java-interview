package proxy.staticproxy;

import proxy.DupSubject;
import proxy.RealSubject;
import proxy.Subject;

/**
 * 静态代理 通过接口方式，实现被代理类实现的接口
 * @description:
 * @author: haochencheng
 * @create: 2019-07-28 12:29
 **/
public class StaticInterfaceProxySubject implements Subject, DupSubject {

    private RealSubject realSubject;

    public StaticInterfaceProxySubject(RealSubject realSubject) {
        this.realSubject = realSubject;
    }

    @Override
    public void speakAgain() {
        before();
        realSubject.speakAgain();
        after();
    }

    @Override
    public void speak() {
        before();
        realSubject.speak();
        after();
    }

    private void before(){
//        System.out.println("before");
    }

    private void after(){
//        System.out.println("after");
    }

    public static void main(String[] args) {
        RealSubject realSubject=new RealSubject();
        StaticInterfaceProxySubject staticInheritProxySubject=new StaticInterfaceProxySubject(realSubject);
        staticInheritProxySubject.speak();
        staticInheritProxySubject.speakAgain();
    }

}
