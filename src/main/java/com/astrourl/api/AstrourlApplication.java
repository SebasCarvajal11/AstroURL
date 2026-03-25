package com.astrourl.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AstrourlApplication {
	public static void main(String[] args) {
		SpringApplication.run(AstrourlApplication.class, args);
	}
}
