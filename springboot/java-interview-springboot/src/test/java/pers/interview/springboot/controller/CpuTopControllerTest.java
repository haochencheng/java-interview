package pers.interview.springboot.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import pers.interview.springboot.common.ResponseResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

class CpuTopControllerTest {

    public static final int COUNT = 50;
    private ExecutorService executorService= Executors.newFixedThreadPool(20);

    private RestTemplate restTemplate=new RestTemplate();

    private final static String REQUEST_URL="http://localhost:8080/cpu/top";

    private static Logger logger = LoggerFactory.getLogger(CpuTopControllerTest.class);

    @Test
    void top() {
        List<Future> futureList=new ArrayList<>();
        for (int i = 0; i < COUNT; i++) {
            Future future =executorService.submit(()->call());
            futureList.add(future);
        }
        for (Future future : futureList) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
    }

    private ResponseResult call() {
        ResponseEntity<ResponseResult>   responseEntity = restTemplate.getForEntity(REQUEST_URL, ResponseResult.class);
        ResponseResult  responseResult = responseEntity.getBody();
        logger.info(responseResult.toString());
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        return responseResult;
    }
}