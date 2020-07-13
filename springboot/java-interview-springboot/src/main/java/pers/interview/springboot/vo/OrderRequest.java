package pers.interview.springboot.vo;

import lombok.Data;

@Data
public class OrderRequest {

    private Integer userId;
    private Integer skuId;
    private String orderNo;

}
