package com.shrescue.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 上海救助基地潜水救生员训练系统 - 后端启动类
 */
@SpringBootApplication(scanBasePackages = "com.shrescue")
public class RescueAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(RescueAdminApplication.class, args);
    }
}
