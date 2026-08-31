package com.kodo.worker.infrastructure.kafka;


import com.kodo.contracts.events.GameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class KafkaEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaEventConsumer.class);

    @KafkaListener(topics = "${kodo.kafka.topic}")
    public void consume(GameEvent event) {
        log.info("Received event: {}", event);
    }
}
