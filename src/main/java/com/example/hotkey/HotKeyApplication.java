package com.example.hotkey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HotKeyApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotKeyApplication.class, args);
    }
}
