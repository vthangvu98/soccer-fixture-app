package com.thangv.SoccerFixturesApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SoccerFixturesAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoccerFixturesAppApplication.class, args);
    }

}
