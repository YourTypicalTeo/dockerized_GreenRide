package com.greenride.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    // Ο Spring "ψεκάζει" (inject) αυτόματα τον KafkaTemplate
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) {
        // Στέλνουμε μήνυμα στο θέμα "login-updates"
        kafkaTemplate.send("login-updates", message);
        System.out.println( "Message sent to Kafka: " + message);
    }
    public void SendMessageToTopic(String topic, String message) {
        kafkaTemplate.send(topic, message);
        System.out.println("Sending to topic " + topic + " : " + message);
    }
}