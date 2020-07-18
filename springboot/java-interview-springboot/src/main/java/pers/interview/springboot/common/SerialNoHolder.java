package pers.interview.springboot.common;

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @description:
 * @author: haochencheng
 * @create: 2020-07-17 22:23
 **/
public class SerialNoHolder {

    private static ThreadLocal<String> threadLocal=new ThreadLocal<>();
    private static final DateTimeFormatter dtf=DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public static String getSerialNo(){
        String serialNo = threadLocal.get();
        if (StringUtils.isEmpty(serialNo)){
            serialNo= LocalDateTime.now().format(dtf) +ThreadLocalRandom.current().nextInt(100);
            threadLocal.set(serialNo);
        }
        return serialNo;
    }

    public static void remove(){
        threadLocal.remove();
    }

}
