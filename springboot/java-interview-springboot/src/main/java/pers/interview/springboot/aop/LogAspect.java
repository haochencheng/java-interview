package pers.interview.springboot.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pers.interview.springboot.common.SerialNoHolder;

/**
 * controller日志切面
 */
@Aspect
@Component
public class LogAspect {

    private final static Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Pointcut("execution(* pers.interview.springboot.controller.*.*(..))")
    public void pointcut() {}

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object result ;
        long beginTime = System.currentTimeMillis();
        String serialNo = SerialNoHolder.getSerialNo();
        try {
            result = point.proceed();
        } catch (Throwable throwable) {
            logger.error("请求错误，流水号：{}",serialNo,throwable);
            throw throwable;
        }finally {
            long endTime=System.currentTimeMillis();
            logger.info("流水号：{} , cost:{}",serialNo,endTime-beginTime);
            SerialNoHolder.remove();
        }
        return result;
    }


}
