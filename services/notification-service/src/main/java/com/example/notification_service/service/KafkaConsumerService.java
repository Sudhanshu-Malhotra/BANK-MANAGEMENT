package com.example.notification_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(topics = "transactionTopic", groupId = "notification-group")
    public void consumeTransactionEvent(String message) {
        log.info("🔔 NOTIFICATION RECEIVED | {}", message);
        // Production extension: integrate email/SMS/push notification service here
        // e.g., notificationClient.send(NotificationRequest.fromMessage(message));
    }
}
