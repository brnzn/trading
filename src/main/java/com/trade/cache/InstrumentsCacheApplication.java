package com.trade.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InstrumentsCacheApplication {
    public static void main(String[] args) {
        SpringApplication.run(InstrumentsCacheApplication.class, args);
    }
}
