package com.greenride;

import com.greenride.service.KafkaProducerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync      //για SMS Notifications
@EnableScheduling //για Ride Reminders
public class GreenrideServiceApplication {

    @Bean
    public CommandLineRunner testKafka(KafkaProducerService producer) {
        return args -> {
            producer.sendMessage("Hello from Docker! Ο Kafka δουλεύει!");
        };
    }
    public static void main(String[] args) {
        SpringApplication.run(GreenrideServiceApplication.class, args);
    }
}