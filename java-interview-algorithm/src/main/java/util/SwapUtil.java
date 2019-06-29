package util;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-06-29 16:55
 **/
public class SwapUtil {

    /**
     * 交换数组
     * @param x 数组
     * @param a
     * @param b
     * @param <T>
     */
    public static <T> void swap(T[] x, int a, int b) {
        T t = x[a];
        x[a] = x[b];
        x[b] = t;
    }
}
