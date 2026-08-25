package com.kodo.api.infrastructure.web.controllers;

import com.kodo.api.domain.dto.requests.CreateEventRequest;
import com.kodo.api.domain.services.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<Void>> createEvent(
            @Valid @RequestBody CreateEventRequest request) {

        return eventService.createEvent(request)
                .thenApply(ignored -> ResponseEntity.accepted().build());

    }
}
