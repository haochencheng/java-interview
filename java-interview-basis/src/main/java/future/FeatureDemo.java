package future;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class FeatureDemo {

    private static Callable callable=() -> {
        Thread.sleep(1000);
        System.out.println("aa");
        return "hello";
    };

    public static void main(String[] args) {
//        futureTask();
        featureCall();
    }

    private static void featureCall(){
        FeatureCall<String> featureCall=new FeatureCall(callable);
        Thread thread=new Thread(featureCall);
        thread.start();
        System.out.println("bb");
        String hello = featureCall.get();
        System.out.println(hello);
    }

    private static void futureTask() {
        FutureTask<String> futureTask=new FutureTask(callable);
        Thread thread=new Thread(futureTask);
        thread.start();
        System.out.println("bb");
        try {
            String hello = futureTask.get();
            System.out.println(hello);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("word");
    }


}
