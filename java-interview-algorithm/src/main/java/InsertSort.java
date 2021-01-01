import java.util.Arrays;

/**
 * 最早拥有排序概念的机器出现在1901至1904年间由Hollerith发明出使用基数排序法的分类机，
 * 此机器系统包括打孔，制表等功能，1908年分类机第一次应用于人口普查，并且在两年内完成了所有的普查数据和归档。
 * Hollerith在1896年创立的分类机公司的前身，为计算机制表记录公司（CTR）。
 * 他在计算机制表记录公司曾担任顾问工程师，直到1921年退休，而计算机制表记录公司在1924年正式改名为IBM。
 *
 * @description:
 * @author: haochencheng
 * @create: 2019-06-29 17:08
 **/
public class InsertSort {


    public static void main(String[] args) {
        Integer[] array = new Integer[]{6, 9, 0, 1, 2, 5, 3, 5};
        sort(array);
        System.out.println(Arrays.deepToString(array));
    }

    public static void sort(Integer[] array) {
        for (int i = 1; i < array.length; i++) {
            int key = array[i];
            int j = i - 1;
            while (j >= 0 && array[j] > key) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = key;
        }
    }


}
