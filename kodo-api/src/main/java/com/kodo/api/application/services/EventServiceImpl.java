package com.kodo.api.application.services;

import com.kodo.api.application.ports.EventPublisher;
import com.kodo.api.domain.dto.requests.CreateEventRequest;
import com.kodo.api.domain.services.EventService;
import com.kodo.contracts.events.GameEvent;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EventServiceImpl implements EventService {

    private final EventPublisher eventPublisher;

    public EventServiceImpl(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CompletableFuture<Void> createEvent(CreateEventRequest request) {

        GameEvent event = new GameEvent(
                request.eventId(),
                request.gameId(),
                request.playerId(),
                request.type(),
                request.occurredAt(),
                request.payload()
        );

        return eventPublisher.publish(event);
    }
}
