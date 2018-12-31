package com.njupt.swg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class SnailmallEurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnailmallEurekaServerApplication.class, args);
    }

}

