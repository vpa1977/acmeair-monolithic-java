package com.acmeair.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@SpringBootConfiguration
@ComponentScan(basePackages = {"com.acmeair"})
public class AcmeAirApp {
   	public static void main(String[] args) {
		SpringApplication.run(AcmeAirApp.class, args);
	}

}
