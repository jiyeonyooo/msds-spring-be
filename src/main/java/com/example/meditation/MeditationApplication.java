package com.example.meditation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.example.meditation", "member", "resv", "room", "global"})
@EntityScan(basePackages = {"com.example.meditation", "member.user.domain", "resv.entity", "room.entity"})
@EnableJpaRepositories(basePackages = {"com.example.meditation", "member.user.repository", "resv.repository", "room.repository"})
public class MeditationApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeditationApplication.class, args);
    }
}
