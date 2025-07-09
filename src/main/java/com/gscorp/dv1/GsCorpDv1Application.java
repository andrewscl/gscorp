package com.gscorp.dv1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class GsCorpDv1Application {

	public static void main(String[] args) {
		SpringApplication.run(GsCorpDv1Application.class, args);
	}

}
