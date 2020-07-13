package pers.interview.springboot.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pers.interview.springboot.vo.OrderRequest;

@Service
public class OrderDao {

    @Autowired
    private JdbcTemplate jdbcTemplate ;

    public int getSkuInventoryLeft(Integer skuId){
        String getSkuInventoryLeftSql="select inventory from sku where id = "+skuId;
        return jdbcTemplate.queryForObject(getSkuInventoryLeftSql, Integer.class);
    }

    /**
     * 直接扣减库存
     * @param skuId
     * @param currentInventory
     * @return
     */
    public boolean subInventory(Integer skuId,int currentInventory){
        String getSkuInventoryLeftSql="update  sku set inventory=? where id = ?";
        int update = jdbcTemplate.update(getSkuInventoryLeftSql, currentInventory - 1, skuId);
        return update>0;
    }

    /**
     * 幂等扣减库存 只有当当前库存没有被修改的情况下 才更新库存，否则重新查询
     * @param skuId
     * @param currentInventory
     * @return
     */
    public boolean subInventoryWithVersion(Integer skuId,int currentInventory){
        String getSkuInventoryLeftSql="update  sku set inventory=? where id = ? and inventory=?";
        int update = jdbcTemplate.update(getSkuInventoryLeftSql, currentInventory - 1, skuId,currentInventory);
        return update>0;
    }

    /**
     * 创建订单
     * @param orderRequest
     */
    public boolean createOrder(OrderRequest orderRequest){
        String createOrderSql="insert into order(user_id,'sku_id','order_no') values (?,?,?)" ;
        int update = jdbcTemplate.update(createOrderSql, orderRequest.getUserId(), orderRequest.getSkuId(),orderRequest.getOrderNo());
        return update>0;
    }


}
