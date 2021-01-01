package concurrent.finaldemo;

/**
 * final 写 重排序
 * @description:
 * @author: haochencheng
 * @create: 2019-12-30 17:46
 **/
public class FinalDemo {

    private int a;
    private final int b;
    static FinalDemo finalDemo;

    public FinalDemo(){
        a=1;
        b=1;
    }

    public static void writer(){
        finalDemo=new FinalDemo();

    }

    public static void reader(){
        FinalDemo finalDemo=FinalDemo.finalDemo;
        System.out.println(finalDemo.a);
        System.out.println(finalDemo.b);
    }

    public static void main(String[] args) {
        Thread thread2=new Thread(()->{
            FinalDemo.writer();
        });
        Thread thread3=new Thread(()->{
            FinalDemo.reader();
        });
        thread2.start();
        thread3.start();
    }


}
