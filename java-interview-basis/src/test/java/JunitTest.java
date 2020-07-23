import org.junit.jupiter.api.Test;
import stream.ListSort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class JunitTest {

    @Test
    public void randomTest() {
        Random random = new Random();
        int randomKey = random.nextInt(10);
        System.out.println(randomKey);
    }

    @Test
    public void listSort() {
        ListSort listSort = new ListSort();
        listSort.setAge(1);
        listSort.setHeight(1);
        listSort.setScore(1);
        ListSort listSort1 = new ListSort();
        listSort1.setAge(1);
        listSort1.setHeight(2);
        listSort1.setScore(1);
        ListSort listSort2 = new ListSort();
        listSort2.setAge(2);
        listSort2.setHeight(2);
        listSort2.setScore(1);
        ListSort listSort3 = new ListSort();
        listSort3.setAge(2);
        listSort3.setHeight(2);
        listSort3.setScore(2);
        List<ListSort> listSortList = Arrays.asList(listSort, listSort1, listSort2, listSort3);
        List<ListSort> collect = listSortList.stream()
                .sorted(
                        Comparator.comparingInt(ListSort::getAge)
                                .thenComparing(ListSort::getHeight)
                                .thenComparing(ListSort::getScore,Comparator.reverseOrder())
                ).collect(Collectors.toList());
        System.out.println(Arrays.deepToString(collect.toArray()));
        return;
    }


}
