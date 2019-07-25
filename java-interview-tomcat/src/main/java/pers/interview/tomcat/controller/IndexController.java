package pers.interview.tomcat.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: haochencheng
 * @create: 2019-07-24 23:05
 **/
@RestController
public class IndexController {

    @GetMapping("/")
    public String index(){
        return "hello,world!";
    }

}
