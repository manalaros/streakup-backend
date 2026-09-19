package com.example.streakup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class StreakUpApplication {

	public static void main(String[] args) {
		SpringApplication.run(StreakUpApplication.class, args);
	}

}
