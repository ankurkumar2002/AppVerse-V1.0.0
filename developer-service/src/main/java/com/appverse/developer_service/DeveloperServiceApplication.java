package com.appverse.developer_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
public class DeveloperServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeveloperServiceApplication.class, args);
	}

}
