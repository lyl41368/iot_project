package com.iot_LYL.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 这是项目的主启动类
 * 用于启动Spring Boot应用程序
 * 包含应用程序的入口点main方法
 */
@SpringBootApplication
@EnableScheduling  // 启用定时任务
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
