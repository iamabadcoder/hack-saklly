package com.yunker.hackx;

import com.yunker.hackx.service.DirectoryWatchService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;


@SpringBootApplication
@ComponentScan
@EnableScheduling
public class HackSakllyApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(HackSakllyApplication.class, args);
//        String[] beanNames = ctx.getBeanNamesForAnnotation(Service.class);
//        System.out.println("Service注解beanNames个数：" + beanNames.length);
        for (String bn : beanNames) {
            System.out.println(bn);
        }
    }

}
