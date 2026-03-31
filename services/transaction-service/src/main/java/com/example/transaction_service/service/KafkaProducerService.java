package com.example.transaction_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String TRANSACTION_TOPIC = "transactionTopic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendTransactionEvent(String message) {
        log.info("Publishing transaction event: {}", message);
        kafkaTemplate.send(TRANSACTION_TOPIC, message)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish transaction event: {}", message, ex);
                } else {
                    log.debug("Transaction event published | partition: {} | offset: {}",
                        result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                }
            });
    }
}
