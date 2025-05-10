package com.appverse.developer_service;

import org.springframework.boot.SpringApplication;

public class TestDeveloperServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(DeveloperServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
