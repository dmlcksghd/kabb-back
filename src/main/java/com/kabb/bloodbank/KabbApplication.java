package com.kabb.bloodbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class KabbApplication {

    public static void main(String[] args) {
        SpringApplication.run(KabbApplication.class, args);
    }

}
