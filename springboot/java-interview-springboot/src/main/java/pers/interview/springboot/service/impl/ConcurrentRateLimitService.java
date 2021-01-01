package pers.interview.springboot.service.impl;

import pers.interview.springboot.service.RateLimitService;

public class ConcurrentRateLimitService implements RateLimitService {

    /**
     * 限流阈值
     */
    private static final int THRESHOLD=5;

    private RateLimit rateLimit=new RateLimit(THRESHOLD,100);

    @Override
    public boolean tryAcquire(long timestamp){
        if (rateLimit.tryAcquire(timestamp)){
            // do something ...
            return true;
        }
        return false;
    }

    class RateLimit {

        /**
         * 当前索引
         */
        private volatile int index;

        private volatile long[] entry;

        /**
         * 请求阈值
         */
        private final int threshold;

        /**
         * 时间窗口 ms
         */
        private final int window;

        public RateLimit(int threshold,int window){
            entry=new long[threshold];
            this.threshold=threshold;
            this.window=window;
        }

        /**
         * 通过数组槽限流 淘汰过期请求
         * 1
         *   2
         *      3
         * 4
         *   5
         *      6
         * @param timestamp 请求时间戳
         * @return
         */
        public boolean tryAcquire(long timestamp){
            if (index<threshold){
                entry[index++]=timestamp;
                return true;
            }
            // 槽满了 判断最早的请求是否过期 过期可以处理新请求
            int nextIndex = index + 1;
            int min = nextIndex % threshold - 1;
            if ((timestamp-entry[min])>window){
                entry[min]=timestamp;
                index=min;
                return true;
            }
            return false;
        }

    }

}
