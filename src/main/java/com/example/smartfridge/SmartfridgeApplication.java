package com.example.smartfridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = SmartfridgeApplication.class)
public class SmartfridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartfridgeApplication.class, args);
    }

}
