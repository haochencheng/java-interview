package proxy.staticproxy;

import proxy.RealSubject;

/**
 * 静态代理 通过继承方式
 * @description:
 * @author: haochencheng
 * @create: 2019-07-28 12:29
 **/
public class StaticInheritProxySubject extends RealSubject {

    @Override
    public void speakAgain() {
        before();
        super.speakAgain();
        after();
    }

    @Override
    public void speak() {
        before();
        super.speak();
        after();
    }

    private void before(){
//        System.out.println("before");
    }

    private void after(){
//        System.out.println("after");
    }

    public static void main(String[] args) {
        StaticInheritProxySubject staticInheritProxySubject=new StaticInheritProxySubject();
        staticInheritProxySubject.speak();
        staticInheritProxySubject.speakAgain();
    }

}
