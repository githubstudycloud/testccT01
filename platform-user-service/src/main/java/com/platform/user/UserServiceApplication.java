package com.platform.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 用户服务启动类
 */
@SpringBootApplication
@EnableJpaAuditing
@EntityScan(basePackages = {"com.platform.shared.domain", "com.platform.user.domain"})
@EnableJpaRepositories(basePackages = "com.platform.user.infrastructure.repository")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}