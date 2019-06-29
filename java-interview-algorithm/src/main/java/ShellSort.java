import util.SwapUtil;

import java.util.Arrays;

/**
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
        //TODO understand
        int number = array.length/2;
        int tmp,i,j;
        while (number>=1){
            for (i = number; i < array.length; i++) {
                tmp = array[i];
                j = i - number;
                while (j>=0 && array[j]>tmp){
                    array[j + number] = array[j];
                    j = j - number;
                }
                array[j + number] = tmp;
            }
            number = number / 2;
        }

    }

}
