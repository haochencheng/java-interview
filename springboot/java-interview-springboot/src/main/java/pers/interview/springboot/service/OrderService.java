package pers.interview.springboot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.interview.springboot.controller.OrderController;
import pers.interview.springboot.dao.OrderDao;
import pers.interview.springboot.exception.InventoryNotEnoughException;
import pers.interview.springboot.vo.OrderRequest;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;


@Service
public class OrderService {

    private static Logger logger = LoggerFactory.getLogger(OrderService.class);

    private HashMap<Integer,SkuInventoryService> skuInventoryServiceHashMap=new HashMap<>();

    private OrderDao orderDao;

    public OrderService(OrderDao orderDao){
        this.orderDao=orderDao;
        skuInventoryServiceHashMap.put(0,(Integer skuId,int currentInventory)-> orderDao.subInventory(skuId,currentInventory));
        skuInventoryServiceHashMap.put(1,(Integer skuId,int currentInventory)-> orderDao.subInventoryWithVersion(skuId,currentInventory));
    }

    /**
     * 下订单服务 直接更新扣减后库存
     * @param orderRequest
     * @return
     */
    @Transactional(rollbackFor = SQLException.class)
    public void order(OrderRequest orderRequest) throws InventoryNotEnoughException {
        createOrder(orderRequest,getSkuInventoryService(0));
    }

    /**
     * 下订单服务 扣减库存幂等
     * @param orderRequest
     * @return
     */
    @Transactional(rollbackFor = SQLException.class)
    public void orderWithVersion(OrderRequest orderRequest) throws InventoryNotEnoughException {
        createOrder(orderRequest,getSkuInventoryService(1));
    }

    private void createOrder(OrderRequest orderRequest, SkuInventoryService skuInventoryService) throws InventoryNotEnoughException {
        Integer skuId = orderRequest.getSkuId();
        int skuLeft;
        do {
            skuLeft = orderDao.getSkuInventoryLeft(skuId);
            logger.info("库存:{}",skuLeft);
            if (skuLeft<=0){
                throw new InventoryNotEnoughException();
            }
            // 扣减库存
        }while (!skuInventoryService.subInventory(skuId,skuLeft));
        // 扣减成功 创建订单
        String orderNo = System.currentTimeMillis() + "" + ThreadLocalRandom.current().nextLong();
        orderRequest.setOrderNo(orderNo);
        orderDao.createOrder(orderRequest);
    }

    private SkuInventoryService getSkuInventoryService(int type){
        return skuInventoryServiceHashMap.get(type);
    }


}
