package com.example.indspringboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class IndSpringBootApplication {

    public static final String APPLICATION_LOCATIONS = "spring.config.location="
            + "classpath:application.yml,"
            + "classpath:aws.yml";


    public static void main(String[] args) {

        new SpringApplicationBuilder(IndSpringBootApplication.class)
                .properties(APPLICATION_LOCATIONS)
                .run(args);
        long heapSize = Runtime.getRuntime().totalMemory();
        System.out.println("HEAP Size(M) : " + heapSize / (1024 * 1024) + " MB");
    }


}
