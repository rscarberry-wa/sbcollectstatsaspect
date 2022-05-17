package com.rscarberry.sbcollectstatsaspect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SbcollectstatsaspectApplication {

	public static void main(String[] args) {
		SpringApplication.run(SbcollectstatsaspectApplication.class, args);
	}

}
