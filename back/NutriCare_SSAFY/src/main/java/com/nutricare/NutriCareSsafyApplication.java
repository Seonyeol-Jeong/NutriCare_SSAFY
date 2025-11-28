package com.nutricare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.nutricare.config.GcsProperties;
@SpringBootApplication
@EnableConfigurationProperties({GcsProperties.class})
public class NutriCareSsafyApplication {

	public static void main(String[] args) {
		SpringApplication.run(NutriCareSsafyApplication.class, args);
	}

}
