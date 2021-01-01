package defaultinterface;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-07-26 00:38
 **/
interface IFoo {
    default void bar(int i) {
        System.out.println("IFoo.bar(int)");
    }
}

public class Foo implements IFoo {

    public static void main(String[] args) {
        Foo foo = new Foo();
        foo.bar(42);  // (1) invokevirtual Foo.bar(int)void

        IFoo ifoo = foo;
        ifoo.bar(42); // (2) invokeinterface IFoo.bar(int)void
    }

    public void bar(long l) {
        System.out.println("Foo.bar(long)");
    }
}