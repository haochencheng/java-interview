
import java.util.Arrays;

/**
 * 希尔排序这个名字，来源于它的发明者希尔，也称作“缩小增量排序”，是插入排序的一种更高效的改进版本。
 *
 * @description:
 * @author: haochencheng
 * @create: 2019-06-29 19:14
 **/
public class ShellSort {

    public static void main(String[] args) {
        Integer[] array = new Integer[]{6, 9, 0, 1, 2, 5, 3, 5};
        sort(array);
        System.out.println(Arrays.deepToString(array));
    }

    private static void sort(Integer[] array) {
        int length = array.length;
        //步长
        int gap = length / 2;
        //i右边元素 索引 ，j 左边元素索引 tmp 临时元素 用于交换
        int i, j, tmp;
        while (gap > 0) {
            //1 2 3 4 5 6 7 8
            // gap = 4
            //   /  i -> 5 6 7 8
            //   / j -> 1 2 3 4
            // gap = 2
            //   /  i ->
            for (i = gap; i < length; i+=gap) {
                System.out.println(i);
                tmp = array[i];
                j = i - gap;
                while (j >= 0 && array[j] > tmp) {
                    array[j + gap] = array[j];
                    j -= gap;
                }
                if (j != i - gap) {
                    array[j + gap] = tmp;
                }
            }
            //步长减半
            gap = gap / 2;
        }
    }

}
