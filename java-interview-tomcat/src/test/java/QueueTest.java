import java.util.Queue;
import java.util.concurrent.*;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-08-02 15:27
 **/
public class QueueTest {

    public static void main(String[] args) throws InterruptedException {
//            BlockingQueue<Integer> queue = new SynchronousQueue<>();
        Queue queue=new LinkedBlockingQueue();
//            Queue queue=new DelayQueue();
            System.out.print(queue.offer(1) + " ");
            System.out.print(queue.offer(2) + " ");
            System.out.print(queue.offer(3) + " ");
            System.out.print(queue.peek() + " ");
            System.out.println(queue.size());
    }

}
