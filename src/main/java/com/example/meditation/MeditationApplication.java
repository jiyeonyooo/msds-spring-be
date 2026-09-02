package com.example.meditation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.example.meditation", "member", "resv", "room", "global", "meditation_program"})
@EntityScan(basePackages = {"com.example.meditation", "member", "resv.entity", "room.entity", "meditation_program"})
@EnableJpaRepositories(basePackages = {"com.example.meditation", "member", "resv.repository", "room.repository", "meditation_program"})
@EnableJpaAuditing
public class MeditationApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeditationApplication.class, args);
    }
}
