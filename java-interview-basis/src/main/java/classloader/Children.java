package classloader;

/**
 * 子类
 * @description:
 * @author: haochencheng
 * @create: 2019-06-23 14:41
 **/
public class Children extends Parent{


    public static int a=1;

    static {
        System.out.println("子类：Children静态代码块"+a);
    }

    public Children(){
        System.out.println("子类：Children 构造器");
    }

    @Override
    public void say() {
        super.say();
        System.out.println("子类：Children say方法");
        super.say();
    }

    public static void main(String[] args) {
        Parent children=new Children();
        children.say();
    }


}
