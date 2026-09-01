package com.kodo.worker.application.services;

import com.kodo.contracts.events.GameEvent;
import com.kodo.worker.application.ports.in.EventProcessor;
import com.kodo.worker.application.ports.out.EventRepository;
import com.kodo.worker.application.validation.GameEventValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventProcessingService implements EventProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(EventProcessingService.class);

    private final EventRepository eventRepository;
    private final GameEventValidator eventValidator;

    public EventProcessingService(EventRepository eventRepository,
                                  GameEventValidator eventValidator) {
        this.eventRepository = eventRepository;
        this.eventValidator = eventValidator;
    }

    @Override
    public void process(GameEvent event) {

        eventValidator.validate(event);

        log.info("Processing event: {}", event);

        eventRepository.save(event);
    }
}
