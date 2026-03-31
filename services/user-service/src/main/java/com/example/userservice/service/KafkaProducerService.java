package com.example.userservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String USER_REGISTRATION_TOPIC = "userRegistrationTopic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendUserRegistrationEvent(Long userId) {
        log.info("Publishing user registration event for userId: {}", userId);
        kafkaTemplate.send(USER_REGISTRATION_TOPIC, userId.toString())
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish user registration event for userId: {}", userId, ex);
                } else {
                    log.info("User registration event published successfully for userId: {} | partition: {} | offset: {}",
                        userId, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                }
            });
    }
}
