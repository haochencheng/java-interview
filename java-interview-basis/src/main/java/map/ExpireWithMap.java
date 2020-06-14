package map;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 带有过期性质得hashMap
 * @param <T>
 */
@Data
public class ExpireWithMap<T> {

    private HashMap<String,ExpireData> expireDataHashMap;

    private ScheduledExecutorService executorService;

    public ExpireWithMap() {
        this.expireDataHashMap = new HashMap<>();
        executorService=Executors.newScheduledThreadPool(1,r -> new Thread(r,"清理线程"));
        executorService.scheduleAtFixedRate(new cleanTask(),1,1, TimeUnit.MINUTES);
    }

    /**
     * 存入键值 带过期时间 s
     * @param key
     * @param data
     * @param expire
     */
    public void put(String key, T data, long expire){
        ExpireData expireData=new ExpireData();
        expireData.setData(data);
        expireData.setKey(key);
        long expireTime = System.currentTimeMillis()+(expire*1000);
        expireData.setExpireTime(expireTime);
        expireDataHashMap.put(key,expireData);
    }

    /**
     * 延迟删除 get得时候判断key 是否过期。存在问题，如果存在大量已经过期key但是没有get，会占用大量内存。
     * @param key
     * @return
     */
    public ExpireData get(String key){
        ExpireData expireData = expireDataHashMap.get(key);
        // 键不存在
        if (Objects.isNull(expireData)){
            return null;
        }
        if (isExpired(expireData)){
            return null;
        }
        return expireData;
    }

    private boolean isExpired(ExpireData expireData) {
        if (expireData.getExpireTime()<System.currentTimeMillis()){
            //已过期
            expireDataHashMap.remove(expireData.getKey());
            return true;
        }
        return false;
    }

    /**
     * 定时清理过期key任务
     */
    class cleanTask implements Runnable {

        @Override
        public void run() {
            System.out.println("开始清理过期key");
            if (expireDataHashMap.size()==0){
                return;
            }
            Set<Map.Entry<String, ExpireData>> entries = expireDataHashMap.entrySet();
            for (Map.Entry<String, ExpireData> entry : entries) {
                ExpireData expireData = entry.getValue();
                if (isExpired(expireData)){
                    System.out.println("清理过期key："+entry.getValue());
                }
            }
        }
    }

    @Data
    class ExpireData {
        private String key;

        private T data;

        private long expireTime;
    }

    public static void main(String[] args) {
        ExpireWithMap expireWithMap=new ExpireWithMap();
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        Runnable putTask = () -> expireWithMap.put("", 2, 10);
        String key="123";
        Runnable getTask = () -> expireWithMap.get(key);
        executorService.submit(putTask);
    }



}
