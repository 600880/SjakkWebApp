package com.example.sjakkwebapp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SjakkWebAppApplication {
	
	private static final int p = Runtime.getRuntime().availableProcessors();
	public static final ExecutorService traadsamling = Executors.newFixedThreadPool(p/2);

	public static void main(String[] args) {
		SpringApplication.run(SjakkWebAppApplication.class, args);
	}

}
