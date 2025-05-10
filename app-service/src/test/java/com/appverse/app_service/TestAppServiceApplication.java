package com.appverse.app_service;

import org.springframework.boot.SpringApplication;

public class TestAppServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(AppServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
