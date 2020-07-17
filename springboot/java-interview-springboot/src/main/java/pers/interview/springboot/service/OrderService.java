package pers.interview.springboot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pers.interview.springboot.dao.OrderDao;
import pers.interview.springboot.exception.InventoryNotEnoughException;
import pers.interview.springboot.vo.OrderRequest;

import java.util.concurrent.ThreadLocalRandom;


@Service
public class OrderService {

    private static Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderDao orderDao;

    /**
     * 下订单服务 直接更新扣减后库存
     * @param orderRequest
     * @return
     */

    public void order(OrderRequest orderRequest) throws InventoryNotEnoughException {
        createOrder(orderRequest,0);
    }

    /**
     * 下订单服务 扣减库存幂等
     * @param orderRequest
     * @return
     */
    public void orderWithVersion(OrderRequest orderRequest) throws InventoryNotEnoughException {
        createOrder(orderRequest,1);
    }

    /**
     * type为1的时候 使用 subInventoryWithVersion 扣减库存
     * 如果使用 @Transactional，spring将数据库auto commit 为false，会造成死锁。
     * 其他线程更新了 库存，因为mysql默认 PR ，没有提交事务读到的始终是 事务开始时候的数据。更新失败造成死锁。
     * 使用 cas 更新后不会产生超卖
     * @param orderRequest
     * @param type
     * @throws InventoryNotEnoughException
     */
//    @Transactional(rollbackFor = SQLException.class)
    public void createOrder(OrderRequest orderRequest,int type) throws InventoryNotEnoughException {
        Integer skuId = orderRequest.getSkuId();
        int skuLeft;
        do {
            skuLeft = orderDao.getSkuInventoryLeft(skuId);
            logger.info("库存:{}",skuLeft);
            if (skuLeft<=0){
                throw new InventoryNotEnoughException();
            }
            // 扣减库存
        }while (!subInventory(skuId,skuLeft,type));
        // 扣减成功 创建订单
        String orderNo = System.currentTimeMillis() + "" + ThreadLocalRandom.current().nextLong();
        orderRequest.setOrderNo(orderNo);
        orderDao.createOrder(orderRequest);
    }

    private boolean subInventory(int skuId,int skuLeft, int type) {
        if (type==0){
            return orderDao.subInventory(skuId,skuLeft);
        }else {
            return orderDao.subInventoryWithVersion(skuId,skuLeft);
        }
    }

}
