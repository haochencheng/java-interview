package pers.interview.springboot.service;

@FunctionalInterface
public interface SkuInventoryService {

    boolean subInventory(Integer skuId,int currentInventory);

}
