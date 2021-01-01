package concurrent.demo;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-12-19 10:52
 **/
public class A {

    /**
     * 0 iconst_0
     * 1 istore_1
     * 2 iinc 1 by 1
     * 5 return
     * @param args
     */
//    public static void main(String[] args) {
//        int i=0;
//        i++;
//    }

    /**
     * 0 iconst_0
     * 1 istore_1
     * 2 iload_1
     * 3 iinc 1 by 1
     * 6 istore_1
     * 7 return
     * @param args
     */
    public static void main(String[] args) {
        int i=0;
        i=i++;
    }

}
