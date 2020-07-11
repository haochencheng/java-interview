package pers.interview.springboot.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import pers.interview.springboot.common.ResponseResult;

import javax.servlet.http.HttpServletRequest;

/**
 * @description:
 * @author: haochencheng
 * @create: 2020-07-11 18:07
 **/
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult<String> sessionNotFoundExceptionHandler(HttpServletRequest request, Exception exception) {
        return ResponseResult.error(exception.getMessage());
    }


}
