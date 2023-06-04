package com.example.nexle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class NexleApplication {

	public static void main(String[] args) {
		SpringApplication.run(NexleApplication.class, args);
	}

}
