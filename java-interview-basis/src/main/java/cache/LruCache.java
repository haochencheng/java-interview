package cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class LruCache<K, V> {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        LruCache<Integer, Integer> lruCache = new LruCache();
        executorService.submit(() -> {
            Random random = new Random();
            for (int i = 0; i < 50; i++) {
                int randomKey = random.nextInt(100);
                lruCache.put(randomKey, 1);
                int getCount = random.nextInt(10);
                for (int j = 0; j < getCount; j++) {
                    lruCache.get(randomKey);
                }
                try {
                    Thread.sleep(randomKey);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        executorService.submit(() -> {
            Random random = new Random();
            for (int i = 0; i < 50; i++) {
                int randomKey = random.nextInt(100);
                lruCache.put(randomKey, 1);
                int getCount = random.nextInt(10);
                for (int j = 0; j < getCount; j++) {
                    lruCache.get(randomKey);
                }
                try {
                    Thread.sleep(randomKey);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        executorService.shutdown();
        System.out.println(lruCache.size);
    }

    private long threshold;
    private long size;
    private ConcurrentHashMap<K, Entry> hashMap;
    private final static ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private final static ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
    private final static ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();

    public LruCache() {
        this.threshold = 16;
        this.size = 0;
        this.hashMap = new ConcurrentHashMap<>(16);
    }

    public LruCache(long threshold) {
        this.threshold = threshold;
        this.size = 0;
        this.hashMap = new ConcurrentHashMap<>(16);
    }

    public void put(K key, V data) {
        if (size >= threshold) {
            // lru 淘汰
            Collection<Entry> values = hashMap.values();
            List collect = values.stream().collect(Collectors.toList());
            Collections.sort(collect);
            Entry entry = (Entry) collect.get(0);
            size--;
            hashMap.remove(entry.getKey());
            System.out.printf("lru 淘汰 key={%d},size={%d},threshold={%d},used={%d}\r\n", key, size, threshold, entry.getUsed());
        }
        size++;
        Entry entry = hashMap.get(key);
        if (Objects.isNull(entry)) {
            entry = new Entry(key, data);
        }
        hashMap.put(key, entry);

    }

    public V get(K key) {
        Entry<V> entry = hashMap.get(key);
        if (Objects.nonNull(entry)) {
            V value = entry.getValue();
            return value;
        }
        return null;
    }

    class Entry<T> implements Comparable<Entry> {

        private K key;
        private T value;

        private Integer used;

        public Entry(K key, T value) {
            this.key = key;
            this.value = value;
            this.used = 0;
        }

        public T getValue() {
            used++;
            return this.value;
        }

        public K getKey() {
            return key;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public Integer getUsed() {
            return used;
        }

        public void addUsed() {
            used++;
        }

        @Override
        public int compareTo(Entry o) {
            return o.getUsed();
        }
    }


}
