package map;


/**
 * @description:
 * @author: haochencheng
 * @create: 2019-06-27 18:25
 **/
public class HashMapDemo {

    public static void main(String[] args) {
        // aka 1292408342
        String a = "sadasdasd";
        String b = "123";
        String c = "das";
        String d = "ghrg";
        String e = "t54tg";
        String f = "svsr2";
        String g = "dfsd";
        String h = "sdfs";
        String i = "23423";
        System.out.println(getIndex(a));
        System.out.println(getIndex(b));
        System.out.println(getIndex(c));
        System.out.println(getIndex(d));
        System.out.println(getIndex(e));
        System.out.println(getIndex(f));
        System.out.println(getIndex(g));
        System.out.println(getIndex(h));
        System.out.println(getIndex(i));
        System.out.println("2|20===="+(2|20));
        System.out.println("2|6===="+(2|6));
    }

    private static int n = 1 << 4;

    private static int getIndex(String key) {
        return ((n - 1) & hash(key));
    }

    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }


}
