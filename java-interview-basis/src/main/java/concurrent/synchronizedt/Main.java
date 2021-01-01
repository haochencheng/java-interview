package concurrent.synchronizedt;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-08-13 23:42
 **/
public class Main {

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        Callable<Boolean> callable1 = () -> {
            new ThreadNew().run();
            return true;
        };
        Callable<Boolean>  callable2 = () -> {
            new ThreadNew().run();
            return true;
        };
        Callable<Boolean>  callable3 = () -> {
            new ThreadNew().run();
            return true;
        };
        List<Callable<Boolean>> callableList = Arrays.asList(callable1, callable2, callable3);
        List<Future<Boolean>> futures = executorService.invokeAll(callableList);
        for (Future<Boolean> future : futures) {
            future.get();
        }
    }

}
