package pers.interview.springboot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pers.interview.springboot.common.ResponseResult;

import java.util.HashSet;
import java.util.Random;

/**
 * 创建大的对象
 * @description:
 * @author: haochencheng
 * @create: 2020-07-11 00:44
 **/
@RestController
@RequestMapping("/big")
public class BigObjectController {

    public static final int ONE_M = 1024*1024;
    private HashSet<byte[]> byteSet=new HashSet<>();

    /**
     * 创建大的对象 不能被回收
     * @return
     */
    @GetMapping("/add")
    public ResponseResult addBigObject(){
        // 1m
        Random random = new Random();
        int count;
        do {
            count = random.nextInt(5);
        }while (count==0);
        int size = ONE_M * count;
        byte[] bytes=new byte[size];
        byteSet.add(bytes);
        return ResponseResult.successful(Integer.valueOf(count));
    }

}
