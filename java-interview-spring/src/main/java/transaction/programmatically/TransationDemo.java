package transaction.programmatically;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;

public class TransationDemo {

    public static void main(String[] args) {
        genProxyByCglibUseProxyFactory();
    }

    /**
     * 使用 ProxyFactory 生成代理类 ，用cglib
     * DefaultAopProxyFactory 决定使用 jdk（实现接口 或者继承 Proxy） 还是 cglib
     */
    private static void genProxyByCglibUseProxyFactory() {
        ProxyFactory proxyFactory=new ProxyFactory();
        proxyFactory.setTarget(new MyServive());
        MyServive myServive = (MyServive)proxyFactory.getProxy();
        myServive.echo();
    }

}
