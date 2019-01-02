package com.njupt.swg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SnailmallCategoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnailmallCategoryServiceApplication.class, args);
    }

}

