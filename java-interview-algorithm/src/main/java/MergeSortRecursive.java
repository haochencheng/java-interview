import java.util.Arrays;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-06-30 12:46
 **/
public class MergeSortRecursive {

    public static void main(String[] args) {
        int[] array = new int[]{6, 9, 0, 1, 2, 5, 3, 5};
//        sort(array);
        mergeSort(array);
        System.out.println(Arrays.toString(array));
    }

    public static void sort(int[] arr) {
        int len = arr.length;
        int[] result = new int[len];
        mergeSortRecursive(arr, result, 0, len - 1);
    }

    /**
     * 递归版本
     *
     * @param arr
     * @param result
     * @param start
     * @param end
     */
    public static void mergeSortRecursive(int[] arr, int[] result, int start, int end) {
        if (start >= end) {
            return;
        }
        //拆分步长
        int len = end - start;
        //拆分中间位置
        int mid = (len >> 1) + start;
        //拆分 左边数组起始位置（指针） 结束位置
        int start1 = start, end1 = mid;
        //拆分 右边数组起始位置（指针） 结束位置
        int start2 = mid + 1, end2 = end;
        //递归 拆分 左边集合
        mergeSortRecursive(arr, result, start1, end1);
        //递归 拆分 右边集合
        mergeSortRecursive(arr, result, start2, end2);
        //临时排序集合起始索引
        int k = start;
        //两边集合比较排序 指针移动
        while (start1 <= end1 && start2 <= end2) {
            result[k++] = arr[start1] > arr[start2] ? arr[start2++] : arr[start1++];
        }
        //放入左边集合 剩余排序元素 到临时排序集合
        while (start1 <= end1) {
            result[k++] = arr[start1++];
        }
        //放右左边集合 剩余排序元素 到临时排序集合
        while (start2 <= end2) {
            result[k++] = arr[start2++];
        }
        //将临时排序数组排序数据 拷贝到源数组
        for (k = start; k <= end; k++) {
            arr[k] = result[k];
        }
    }

    /**
     * 迭代版本
     *
     * @param arr
     */
    public static void mergeSort(int[] arr) {
        int[] orderedArr = new int[arr.length];
        //i是拆分步长
        for (int i = 2; i < arr.length * 2; i *= 2) {
            for (int j = 0; j < (arr.length + i - 1) / i; j++) {
                //拆分左边集合开始索引
                int left = i * j;
                //拆分左边集合结束索引
                int mid = left + i / 2 >= arr.length ? arr.length - 1 : left + i / 2;
                //拆分右边集合结束索引
                // 1 2 3 4 5 6 7 8
                // 12 34 56 78
                // 1234 5678
                // 1 2 3 4 5 6 7 8
                int right = i * (j + 1) - 1 >= arr.length ? (arr.length - 1) : (i * (j + 1) - 1);
                //start 新排序集合的指针 l 拆分后左边 集合指针 m 拆分后右边集合指针
                int start = left, l = left, m = mid;
                //排序 左、右集合 通过指针移动 放入 临时排序数组
                while (l < mid && m <= right) {
                    if (arr[l] < arr[m]) {
                        orderedArr[start++] = arr[l++];
                    } else {
                        orderedArr[start++] = arr[m++];
                    }
                }
                //将左边集合 剩余排序元素 放入 临时排序数组
                while (l < mid) {
                    orderedArr[start++] = arr[l++];
                }
                //将右边集合 剩余排序元素 放入 临时排序数组
                while (m <= right) {
                    orderedArr[start++] = arr[m++];
                }
                //将排序完成数据 拷贝到原有数组
                System.arraycopy(orderedArr, left, arr, left, right - left + 1);
            }
        }
    }


}
