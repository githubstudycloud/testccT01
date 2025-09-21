package com.platform.external;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 外部API服务启动类
 */
@SpringBootApplication
@EnableCaching
public class ExternalApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExternalApiApplication.class, args);
    }
}