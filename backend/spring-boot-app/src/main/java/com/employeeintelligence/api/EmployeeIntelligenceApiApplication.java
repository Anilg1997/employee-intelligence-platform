package com.employeeintelligence.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.employeeintelligence")
@EnableScheduling
public class EmployeeIntelligenceApiApplication {

	private static final String HTTP_CLIENT_BUILDER_FACTORY =
			"langchain4j.http.clientBuilderFactory";
	private static final String DEFAULT_HTTP_CLIENT_BUILDER_FACTORY =
			"dev.langchain4j.http.client.jdk.JdkHttpClientBuilderFactory";

	static {
		String factory = System.getProperty(HTTP_CLIENT_BUILDER_FACTORY);
		if (factory == null || factory.isBlank()) {
			factory = System.getenv("LANGCHAIN4J_HTTP_CLIENT_BUILDER_FACTORY");
		}
		if (factory == null || factory.isBlank()) {
			factory = DEFAULT_HTTP_CLIENT_BUILDER_FACTORY;
		}
		System.setProperty(HTTP_CLIENT_BUILDER_FACTORY, factory);
	}

	public static void main(String[] args) {
		SpringApplication.run(EmployeeIntelligenceApiApplication.class, args);
	}

}
