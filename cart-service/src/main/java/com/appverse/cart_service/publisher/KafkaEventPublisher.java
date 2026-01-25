package com.appverse.cart_service.publisher;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishAfterCommit(String topic, String key, Object payload){

        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    kafkaTemplate.send(topic, key, payload);
                    log.info(
                        "Kafka event published | topic={} | key={} | payloadType={}",
                        topic, key, payload.getClass().getSimpleName()
                    );
                }
            }
        );
    }
}
