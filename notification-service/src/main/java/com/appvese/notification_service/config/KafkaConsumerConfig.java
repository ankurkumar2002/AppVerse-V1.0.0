package com.appvese.notification_service.config;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.appvese.notification_service.event.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DomainEvent<?>>
    kafkaListenerContainerFactory(ObjectMapper objectMapper) {

        JsonDeserializer<DomainEvent<?>> deserializer =
                new JsonDeserializer<>(DomainEvent.class, objectMapper);

        deserializer.addTrustedPackages("*");

        DefaultKafkaConsumerFactory<String, DomainEvent<?>> factory =
                new DefaultKafkaConsumerFactory<>(
                        Map.of(
                            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
                            ConsumerConfig.GROUP_ID_CONFIG, "notification-service"
                        ),
                        new StringDeserializer(),
                        deserializer
                );

        ConcurrentKafkaListenerContainerFactory<String, DomainEvent<?>> listenerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();

        listenerFactory.setConsumerFactory(factory);
        return listenerFactory;
    }
}
