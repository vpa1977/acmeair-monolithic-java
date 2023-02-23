package com.acmeair.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@SpringBootConfiguration
@ComponentScan(basePackages = {"com.acmeair"})
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class})
public class AcmeAirApp {
   	public static void main(String[] args) {
		SpringApplication.run(AcmeAirApp.class, args);
	}

}
