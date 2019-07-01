
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
        int gap = length / 2;
        int i, j, tmp;
        while (gap > 0) {
            for (i = gap; i < length; i++) {
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
            gap = gap / 2;
        }
    }

}
