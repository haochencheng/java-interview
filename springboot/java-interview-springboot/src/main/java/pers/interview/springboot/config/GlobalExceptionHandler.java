package pers.interview.springboot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import pers.interview.springboot.common.ResponseResult;
import pers.interview.springboot.controller.CpuTopController;

import javax.servlet.http.HttpServletRequest;

/**
 * @description:
 * @author: haochencheng
 * @create: 2020-07-11 18:07
 **/
@ControllerAdvice
public class GlobalExceptionHandler {
    private static Logger logger = LoggerFactory.getLogger(CpuTopController.class);

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult<String> sessionNotFoundExceptionHandler(HttpServletRequest request, Exception exception) {
        logger.error("程序错误",exception);
        return ResponseResult.error(exception.getMessage());
    }


}
