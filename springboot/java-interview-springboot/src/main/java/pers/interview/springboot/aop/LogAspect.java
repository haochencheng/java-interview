package pers.interview.springboot.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAspect {

    private final static Logger logger = LoggerFactory.getLogger(LogAspect.class);

    //表示匹配带有自定义注解的方法
    @Pointcut("execution(* pers.interview.springboot.controller.*.*(..))")
    public void pointcut() {}

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object result ;
        long beginTime = System.currentTimeMillis();
        try {
            result = point.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw throwable;
        }finally {
            long endTime=System.currentTimeMillis();
            logger.info("cost:{}",endTime-beginTime);
        }
        return result;
    }


}
