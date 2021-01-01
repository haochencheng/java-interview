import util.SwapUtil;

import java.util.Arrays;

/**
 * 选择排序
 * 首先在未排序序列中找到最小（大）元素，存放到排序序列的起始位置，然后，再从剩余未排序元素中继续寻找最小（大）元素，
 * 然后放到已排序序列的末尾。以此类推，直到所有元素均排序完毕。
 *
 * @description:
 * @author: haochencheng
 * @create: 2019-06-29 16:40
 **/
public class SelectionSort {

    public static void main(String[] args) {
        Integer[] array = new Integer[]{6, 9, 0, 1, 2, 5, 3, 5};
        sort(array);
        System.out.println(Arrays.deepToString(array));
    }

    private static void sort(Integer[] array) {
        for (int i = 0; i < array.length; i++) {
            // 最小值索引
            int minIndex = i;
            for (int j = i + 1; j < array.length; j++) {
                minIndex = array[minIndex] < array[j] ? minIndex : j;
            }
            // 将未排序列中最小元素放到已排序列末尾
            if (minIndex!=i){
                SwapUtil.swap(array, i, minIndex);
            }
        }
    }

}
