package pers.interview.springboot.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Order {

    private Integer id;
    private Integer skuId;
    private String orderNo;
    private LocalDateTime createTime;

}
