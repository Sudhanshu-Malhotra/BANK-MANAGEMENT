package com.example.accountservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private AccountService accountService;

    @KafkaListener(topics = "userRegistrationTopic", groupId = "account-group")
    public void consumeUserRegistrationEvent(String userIdString) {
        log.info("Received user registration event for userId: {}", userIdString);
        try {
            Long userId = Long.parseLong(userIdString);
            accountService.createAccount(userId);
            log.info("Auto-created banking account for userId: {}", userId);
        } catch (NumberFormatException e) {
            log.error("Invalid userId format received: '{}'. Skipping event.", userIdString, e);
        } catch (Exception e) {
            log.error("Failed to create account for userId: {}. Reason: {}", userIdString, e.getMessage(), e);
        }
    }
}
