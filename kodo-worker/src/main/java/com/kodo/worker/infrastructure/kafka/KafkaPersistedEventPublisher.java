package com.kodo.worker.infrastructure.kafka;

import com.kodo.contracts.events.GameEventPersisted;
import com.kodo.worker.application.ports.out.PersistedEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class KafkaPersistedEventPublisher implements PersistedEventPublisher {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final String topic;

    public KafkaPersistedEventPublisher(
            KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${kodo.kafka.persisted-topic}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(GameEventPersisted event) {
        try {
            kafkaTemplate
                    .send(topic, event.gameId(), event)
                    .get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing persisted event", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to publish persisted event", e.getCause());
        }
    }
}