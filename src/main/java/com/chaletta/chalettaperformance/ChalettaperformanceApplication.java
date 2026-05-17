package com.chaletta.chalettaperformance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChalettaperformanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChalettaperformanceApplication.class, args);
	}

}
