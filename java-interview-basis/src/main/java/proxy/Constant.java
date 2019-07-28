package proxy;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-07-28 09:40
 **/
public class Constant {

    public static final Integer W=10_000;
    public static final Integer TEN_W=100_000;
    public static final Integer TH_W=10_000_000;

    public static final Boolean debug=false;

    public static void debug() throws InterruptedException {
        if (Constant.debug){
            for (;;){
                Thread.sleep(1000);
            }
        }
    }
}
