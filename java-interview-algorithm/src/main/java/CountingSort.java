import java.util.Arrays;

/**
 * 计数排序
 * @description:
 * @author: haochencheng
 * @create: 2019-07-03 15:31
 **/
public class CountingSort {

    public static void main(String[] args) {
        int[] array = new int[]{6, 9, 0, 1, 2, 5, 3, 5};
        int[] sort = sort(array);
        System.out.println(Arrays.toString(sort));
    }

    public static int[] sort(int[] A) {
        int[] B = new int[A.length];
        // 假设A中的数据a'有，0<=a' && a' < k并且k=100
        int k = 100;
        countingSort(A, B, k);
        return B;
    }



    private static void countingSort(int[] A, int[] B, int k) {
        int[] C = new int[k];
        // 计数
        for (int j = 0; j < A.length; j++) {
            int a = A[j];
            C[a] += 1;
        }
        // 求计数和
        for (int i = 1; i < k; i++) {
            C[i] = C[i] + C[i - 1];
        }
        // 整理
        for (int j = A.length - 1; j >= 0; j--) {
            int a = A[j];
            B[C[a] - 1] = a;
            C[a] -= 1;
        }
    }

//    private static void countingSort(int[] origin, int[] tmp, int max) {
//        int[] c = new int[max+1];
//        // 计数
//        for (int j = 0; j < origin.length; j++) {
//            c[origin[j]]+=1;
//        }
//        // 求计数和
//        for (int i = 1; i < max; i++) {
//            origin[i]=origin[i]+origin[i-1];
//        }
//        // 整理
//        for (int j = origin.length - 1; j >= 0; j--) {
//            int a = origin[j];
//            tmp[c[a] - 1] = a;
//            c[a] -= 1;
//        }
//    }


}
