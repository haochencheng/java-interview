package pers.interview.springboot.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import pers.interview.springboot.common.ResponseResult;
import pers.interview.springboot.vo.OrderRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class OrderControllerTest {

    private static Logger logger = LoggerFactory.getLogger(OrderControllerTest.class);

    public static final int COUNT = 200;
    private ExecutorService executorService= Executors.newFixedThreadPool(50);

    private RestTemplate restTemplate=new RestTemplate();

    private final static String REQUEST_URL="http://localhost:8080/order/make";
    private final static String REQUEST_URL_VERSION="http://localhost:8080/order/makeV";

    @Test
    void order() {
        createOrder(REQUEST_URL);
    }

    private ResponseResult call(String requestUrl, OrderRequest orderRequest) {
        //设置Http Header
        HttpHeaders headers = new HttpHeaders();
        //设置请求媒体数据类型
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderRequest> formEntity = new HttpEntity<>(orderRequest, headers);
        ResponseEntity<ResponseResult> responseEntity = restTemplate.postForEntity(requestUrl,formEntity, ResponseResult.class);
        ResponseResult  responseResult = responseEntity.getBody();
        logger.info(responseResult.toString());
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        return responseResult;
    }

    @Test
    void orderWithVersion() {
        createOrder(REQUEST_URL_VERSION);
    }

    private void createOrder(String requestUrlVersion) {
        List<Future> futureList = new ArrayList<>();
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setSkuId(1);
//        for (int i = 0; i < COUNT; i++) {
        for (int i = 0; i < 3; i++) {
            orderRequest.setUserId(i);
            Future future = executorService.submit(() -> call(requestUrlVersion, orderRequest));
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
}