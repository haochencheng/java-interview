package pers.interview.springboot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.interview.springboot.common.ResponseResult;

import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

/**
 * cpu高负载
 * @description:
 * @author: haochencheng
 * @create: 2020-07-11 00:44
 **/
@RestController
@RequestMapping("/cpu")
public class CpuTopController {

    private static Logger logger = LoggerFactory.getLogger(CpuTopController.class);

    /**
     * 随机休眠，占用线程。模拟线程阻塞
     * @return
     */
    @GetMapping("/top")
    public ResponseResult top(){
        int delay ;
        do {
            delay=ThreadLocalRandom.current().nextInt(10);
        }while (delay==0);
        logger.info("delay",delay);
        try {
            Thread.sleep(delay*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ResponseResult.successful();
    }

}
