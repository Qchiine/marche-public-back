package com.emsi.marches_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class MarchesBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarchesBackendApplication.class, args);
	}

}
