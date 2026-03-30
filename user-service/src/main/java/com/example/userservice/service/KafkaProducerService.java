package com.example.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendUserRegistrationEvent(Long userId) {
        kafkaTemplate.send("userRegistrationTopic", userId.toString());
        System.out.println("Sent userRegistrationTopic event for User ID: " + userId);
    }
}
