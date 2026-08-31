package com.kodo.worker.infrastructure.kafka;


import com.kodo.contracts.events.GameEvent;
import com.kodo.worker.application.ports.in.EventProcessor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class KafkaEventConsumer {

    private final EventProcessor eventProcessor;

    public KafkaEventConsumer(EventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    @KafkaListener(topics = "${kodo.kafka.topic}")
    public void consume(GameEvent event) {
        eventProcessor.process(event);
    }
}
