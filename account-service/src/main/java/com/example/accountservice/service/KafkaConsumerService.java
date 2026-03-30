package com.example.accountservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @Autowired
    private AccountService accountService;

    @KafkaListener(topics = "userRegistrationTopic", groupId = "account-group")
    public void consumeUserRegistrationEvent(String userIdString) {
        System.out.println("Received userRegistrationTopic event for User ID: " + userIdString);
        try {
            Long userId = Long.parseLong(userIdString);
            accountService.createAccount(userId);
            System.out.println("Auto-created banking account for User ID: " + userId);
        } catch (Exception e) {
            System.err.println("Failed to process event: " + e.getMessage());
        }
    }
}
