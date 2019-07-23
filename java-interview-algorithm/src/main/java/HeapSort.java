import util.SwapUtil;

import java.util.Arrays;

/**
 * 堆排序
 * @description:
 * @author: haochencheng
 * @create: 2019-07-02 13:26
 **/
public class HeapSort {

    public static void main(String[] args) {
        int[] array = new int[]{6, 9, 0, 1, 2, 5, 3, 5};
        sort(array);
        System.out.println(Arrays.toString(array));
    }

    private static void sort(int[] array) {
        /*
         *  第一步：将数组堆化
         *  beginIndex = 第一个非叶子节点。
         *  从第一个非叶子节点开始即可。无需从最后一个叶子节点开始。
         *  叶子节点可以看作已符合堆要求的节点，根节点就是它自己且自己以下值为最大。
         */
        int len = array.length - 1;
        int beginIndex=len >> 1;
        for (int i = beginIndex; i >=0 ; i--) {
            maxHeapify(i, len,array);
        }
        /*
         * 第二步：对堆化数据排序
         * 每次都是移出最顶层的根节点A[0]，与最尾部节点位置调换，同时遍历长度 - 1。
         * 然后从新整理被换到根节点的末尾元素，使其符合堆的特性。
         * 直至未排序的堆长度为 0。
         */
        for (int i = len; i > 0; i--) {
            SwapUtil.swap(array,0, i);
            maxHeapify(0, i - 1,array);
        }
    }

    /**
     * 调整索引为 index 处的数据，使其符合堆的特性。
     *
     * @param index 需要堆化处理的数据的索引
     * @param len 未排序的堆（数组）的长度
     */
    private static void maxHeapify(int index, int len, int[] array) {
        // 左子节点索引
        int li=index << 1 + 1 ;
        // 右子节点索引
        int ri=index +1;
        // 子节点值最大索引，默认左子节点。
        int cMax=li;
        // 左子节点索引超出计算范围，直接返回。
        if (li>len){
            return;
        }
        // 先判断左右子节点，哪个较大。
        if (ri <= len && array[ri]>array[li]){
            cMax = ri;
        }
        if (array[cMax]>array[index]){
            // 如果父节点被子节点调换，
            SwapUtil.swap(array, cMax, index);
            // 则需要继续判断换下后的父节点是否符合堆的特性。
            maxHeapify(cMax, len,array);
        }

    }


}
