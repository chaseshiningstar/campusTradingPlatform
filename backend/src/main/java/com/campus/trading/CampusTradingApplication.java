package com.campus.trading;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园二手交易平台启动类
 */
@SpringBootApplication
@MapperScan("com.campus.trading.mapper")
public class CampusTradingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusTradingApplication.class, args);
        System.out.println("========================================");
        System.out.println("校园二手交易平台启动成功!");
        System.out.println("API文档地址: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}
