package com.employeeintelligence.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EmployeeIntelligenceApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeeIntelligenceApiApplication.class, args);
	}

}
