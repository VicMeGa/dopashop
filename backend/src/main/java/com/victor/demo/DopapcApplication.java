package com.victor.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.security.SecureRandom;

@SpringBootApplication
public class DopapcApplication {

	public static void main(String[] args) {
		SpringApplication.run(DopapcApplication.class, args);
	}

	@Bean
	public SecureRandom secureRandom() {
		return new SecureRandom();
	}

}
