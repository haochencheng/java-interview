package pers.interview.springboot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.interview.springboot.common.ResponseResult;
import pers.interview.springboot.exception.InventoryNotEnoughException;
import pers.interview.springboot.service.OrderService;
import pers.interview.springboot.vo.OrderRequest;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 并发测试类
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    private static Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    /**
     * 并发下订单
     * @return
     */
    @GetMapping("/make")
    public ResponseResult order(@RequestBody OrderRequest orderRequest){
        try {
            orderService.order(orderRequest);
        } catch (InventoryNotEnoughException e) {
            logger.info("库存不足");
            return ResponseResult.error(-100,"库存不足");
        }
        return ResponseResult.successful(orderRequest.getOrderNo());
    }

    /**
     * 并发下订单
     * @return
     */
    @GetMapping("/makeV")
    public ResponseResult orderWithVersion(@RequestBody OrderRequest orderRequest){
        try {
            orderService.orderWithVersion(orderRequest);
        } catch (InventoryNotEnoughException e) {
            logger.info("库存不足");
            return ResponseResult.error(-100,"库存不足");
        }
        return ResponseResult.successful(orderRequest.getOrderNo());
    }

}
