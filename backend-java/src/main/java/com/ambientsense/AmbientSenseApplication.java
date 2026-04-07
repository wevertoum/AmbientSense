package com.ambientsense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AmbientSenseApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmbientSenseApplication.class, args);
    }
}
