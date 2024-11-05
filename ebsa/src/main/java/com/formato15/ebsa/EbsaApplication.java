package com.formato15.ebsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.formato15.ebsa.*")
public class EbsaApplication {

	public static void main(String[] args) {
		SpringApplication.run(EbsaApplication.class, args);
	}

}
