package com.kodo.api.infrastructure.kafka;

import com.kodo.api.application.ports.out.EventPublisher;
import com.kodo.contracts.events.GameEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, GameEvent> kafkaTemplate;
    private final String topic;

    public KafkaEventPublisher(
            KafkaTemplate<String, GameEvent> kafkaTemplate,
            @Value("${kodo.kafka.topic}") String topic) {

        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public CompletableFuture<Void> publish(GameEvent event) {
        return kafkaTemplate
                .send(topic, event)
                .thenApply(result -> null);
    }
}
