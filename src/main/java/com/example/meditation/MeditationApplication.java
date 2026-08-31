package com.example.meditation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.example.meditation", "resv"})
@EntityScan(basePackages = "resv.entity")
@EnableJpaRepositories(basePackages = "resv.repository")
public class MeditationApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeditationApplication.class, args);
	}

}
