import java.util.Arrays;

/**
 * 快速排序
 * @description:
 * @author: haochencheng
 * @create: 2019-07-01 23:53
 **/
public class QuitSort {

    public static void main(String[] args) {
        int[] array = new int[]{6, 9, 0, 1, 2, 5, 3, 5};
        sort(array);
        System.out.println(Arrays.toString(array));
    }

    private static void sort(int[] array) {
        quickSort(array, 0, array.length - 1);
    }

    private static void quickSort(int[] array, int head, int tail) {
        if (head >= tail || array == null || array.length <= 1) {
            return;
        }
        int pivot = array[(head + tail) / 2];
        int i = head, j = tail;
        while (i <= j) {
            while (array[i] < pivot) {
                i++;
            }
            while (array[j] > pivot) {
                j--;
            }
            if (i < j) {
                int tmp = array[i];
                array[i] = array[j];
                array[j] = tmp;
                ++i;
                --j;
            } else if (i == j) {
                i++;
            }
        }
        quickSort(array, head, j);
        quickSort(array, i, tail);
    }

}
