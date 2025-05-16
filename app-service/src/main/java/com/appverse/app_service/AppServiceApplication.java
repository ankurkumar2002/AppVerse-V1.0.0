package com.appverse.app_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AppServiceApplication {

	public static void main(String[] args) { 
		SpringApplication.run(AppServiceApplication.class, args);
	}

}
