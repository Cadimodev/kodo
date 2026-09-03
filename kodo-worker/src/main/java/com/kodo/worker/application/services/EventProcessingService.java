package com.kodo.worker.application.services;

import com.kodo.contracts.events.GameEvent;
import com.kodo.worker.application.ports.in.EventProcessor;
import com.kodo.worker.application.ports.out.EventRepository;
import com.kodo.worker.application.ports.out.OutboxRepository;
import com.kodo.worker.application.validation.GameEventValidator;
import com.kodo.worker.infrastructure.postgres.entities.OutboxEventEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventProcessingService implements EventProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(EventProcessingService.class);

    private final OutboxRepository outboxRepository;
    private final EventRepository eventRepository;
    private final GameEventValidator eventValidator;

    public EventProcessingService(OutboxRepository outboxRepository,
                                  EventRepository eventRepository,
                                  GameEventValidator eventValidator) {
        this.outboxRepository = outboxRepository;
        this.eventRepository = eventRepository;
        this.eventValidator = eventValidator;
    }

    @Override
    @Transactional
    public void process(GameEvent event) {

        eventValidator.validate(event);

        log.info("Processing event: {}", event);

        boolean inserted = eventRepository.saveIfAbsent(event);

        if (inserted) {
            outboxRepository.savePending(event);
        }
    }
}
