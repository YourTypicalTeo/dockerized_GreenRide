package com.greenride.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    // Ακούει στο ίδιο θέμα "login-updates"
    @KafkaListener(topics = "login-updates", groupId = "greenride-group")
    public void listen(String message) {
        System.out.println("Received Message: " + message);
    }
}
