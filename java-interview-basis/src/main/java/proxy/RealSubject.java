package proxy;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-07-25 14:20
 **/
public class RealSubject implements Subject, DupSubject {


    @Override
    public void speakAgain() {
        System.out.println("DupSubject speakAgain");
    }

    @Override
    public void speak() {
//        System.out.println("Subject speak");
    }
}
