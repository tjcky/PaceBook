package com.pacebookcorp.doragee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Kwon Young
 */
@SpringBootApplication
@ComponentScan
public class PaceBookApplication {
	public static void main(String[] args) {
		SpringApplication.run(PaceBookApplication.class, args);
	}
}