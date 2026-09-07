package com.kodo.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class KodoWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KodoWorkerApplication.class, args);
	}
}