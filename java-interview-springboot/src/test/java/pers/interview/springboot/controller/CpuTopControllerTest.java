package pers.interview.springboot.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import pers.interview.springboot.common.ResponseResult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class CpuTopControllerTest {

    private ExecutorService executorService= Executors.newFixedThreadPool(10);

    private RestTemplate restTemplate=new RestTemplate();

    private final static String REQUEST_URL="http://localhost:8080/cpu/top";

    private static Logger logger = LoggerFactory.getLogger(CpuTopControllerTest.class);

    @Test
    void top() {
        for (int i = 0; i < 100; i++) {
            executorService.execute(()->call());
        }
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void call() {
        ResponseEntity<ResponseResult> responseEntity;
        ResponseResult responseResult;
        do {
            responseEntity = restTemplate.getForEntity(REQUEST_URL, ResponseResult.class);
            responseResult = responseEntity.getBody();
            logger.info(responseResult.toString());
            Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        }while (responseResult.isSuccess());
    }
}