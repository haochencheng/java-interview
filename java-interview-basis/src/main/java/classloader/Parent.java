package classloader;

/**
 * 父类
 * @description:
 * @author: haochencheng
 * @create: 2019-06-23 14:40
 **/
public class Parent {

    public static int A=1;

    static {
        System.out.println("父类：Parent 静态代码块"+A);
    }

    public Parent(){
        System.out.println("父类：Parent 构造器");
    }

    public void say(){
        System.out.println("父类：Parent 方法say");
    }

}
