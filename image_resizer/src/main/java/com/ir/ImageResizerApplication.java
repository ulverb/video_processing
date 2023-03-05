package com.ir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class ImageResizerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageResizerApplication.class, args);
    }
}
