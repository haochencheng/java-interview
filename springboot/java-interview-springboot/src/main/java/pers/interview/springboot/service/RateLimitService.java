package pers.interview.springboot.service;

public interface RateLimitService {

    boolean tryAcquire(long timestamp);

}
