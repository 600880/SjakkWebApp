package com.example.sjakkwebapp.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    @Bean
    public ExecutorService gameExecutor() {
        // Creates a thread pool that reuses a fixed number of threads
        // Adjust the number (e.g., 4) based on your server capacity
        return Executors.newFixedThreadPool(4);
    }
}
