package com.guzem.uzaktan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UzaktanApplication {

	public static void main(String[] args) {
		SpringApplication.run(UzaktanApplication.class, args);
	}

}
