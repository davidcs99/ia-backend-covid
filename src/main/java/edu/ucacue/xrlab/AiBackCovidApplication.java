package edu.ucacue.xrlab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AiBackCovidApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiBackCovidApplication.class, args);
	}

}
