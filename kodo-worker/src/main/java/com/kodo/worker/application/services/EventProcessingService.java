package com.kodo.worker.application.services;

import com.kodo.contracts.events.GameEvent;
import com.kodo.worker.application.ports.in.EventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventProcessingService implements EventProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(EventProcessingService.class);

    @Override
    public void process(GameEvent event) {
        log.info("Processing event: {}", event);
    }
}
