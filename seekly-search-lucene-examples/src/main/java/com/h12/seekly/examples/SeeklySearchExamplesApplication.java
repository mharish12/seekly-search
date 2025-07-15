package com.h12.seekly.examples;

import com.h12.seekly.examples.service.SeeklyDemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class SeeklySearchExamplesApplication {

    private final SeeklyDemoService demoService;

    public SeeklySearchExamplesApplication(SeeklyDemoService demoService) {
        this.demoService = demoService;
    }

    public static void main(String[] args) {
        SpringApplication.run(SeeklySearchExamplesApplication.class, args);
    }

    @Bean
    public CommandLineRunner demoRunner() {
        return args -> {
            log.info("Starting Seekly Search Examples Demo...");
            demoService.runLuceneDemo();
        };
    }
}