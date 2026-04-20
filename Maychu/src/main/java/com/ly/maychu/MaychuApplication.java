package com.ly.maychu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
@SpringBootApplication
@EnableScheduling
public class MaychuApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaychuApplication.class, args);
    }
    @Bean
    public CommandLineRunner generateHash(PasswordEncoder encoder) {
        return args -> {
            System.out.println("========================================");
            System.out.println("HASH CHUẨN CỦA 123456 LÀ:");
            System.out.println(encoder.encode("123456"));
            System.out.println("========================================");
        };
    }
}
