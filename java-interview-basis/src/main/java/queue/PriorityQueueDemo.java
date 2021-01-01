package queue;

import javafx.scene.layout.Priority;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class PriorityQueueDemo {

    public static void main(String[] args) {
        PriorityQueue<Integer> priority=new PriorityQueue();
        priority.add(9);
        priority.add(4);
        priority.add(6);
        priority.add(2);
        int size = priority.size();
        for (int i = 0; i < size; i++) {
            System.out.println(priority.poll());
        }
    }

}
