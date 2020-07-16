package pers.interview.springboot.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import pers.interview.springboot.common.ResponseResult;

import java.util.Random;


class BigObjectControllerTest {

    private static Logger logger = LoggerFactory.getLogger(BigObjectControllerTest.class);

    private RestTemplate restTemplate=new RestTemplate();

    private final static String ADD_URL="http://localhost:8080/big/add";

    @Test
    @DisplayName("Test assert java.lang.OutOfMemoryError")
    void addBigObject() {
        Executable closureContainingCodeToTest = () -> {
            ResponseEntity<ResponseResult> responseEntity;
            ResponseResult responseResult;
            Random random = new Random();
            do {
                responseEntity = restTemplate.getForEntity(ADD_URL, ResponseResult.class);
                responseResult = responseEntity.getBody();
                logger.info(responseResult.toString());
                Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
                Thread.sleep(random.nextInt(1000));
            }while (responseResult.isSuccess());
        };
        Assertions.assertThrows(HttpServerErrorException.InternalServerError.class, closureContainingCodeToTest, "java.lang.OutOfMemoryError");
    }

}