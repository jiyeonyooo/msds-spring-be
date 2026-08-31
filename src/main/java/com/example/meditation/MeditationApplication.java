package com.example.meditation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.meditation", "room", "global"})
@EntityScan(basePackages = "room.entity")
@EnableJpaRepositories(basePackages = "room.repository")
public class MeditationApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeditationApplication.class, args);
	}

}
