package com.example.webmuasam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableFeignClients
@EnableAsync
@EnableTransactionManagement

public class WebmuasamApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebmuasamApplication.class, args);
	}

}
