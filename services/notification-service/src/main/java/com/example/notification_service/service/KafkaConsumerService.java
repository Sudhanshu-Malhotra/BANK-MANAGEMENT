package com.example.notification_service.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "transactionTopic", groupId = "notification-group")
    public void consumeTransactionEvent(String message) {
        System.out.println("==================================================");
        System.out.println("🔔 NOTIFICATION ALERT: " + message);
        System.out.println("==================================================");
    }
}
