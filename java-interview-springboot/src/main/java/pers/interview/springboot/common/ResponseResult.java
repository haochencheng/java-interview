package pers.interview.springboot.common;

import lombok.Data;

/**
 * @description:
 * @author: haochencheng
 * @create: 2020-07-11 00:49
 **/
@Data
public class ResponseResult<T> {

    private ResponseResult(){

    }

    private int code=0;

    private String msg;

    private T data;

    public static ResponseResult successful(){
        return new ResponseResult();
    }

}
