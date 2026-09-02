package com.kodo.api.infrastructure.web.controllers;

import com.kodo.api.application.dto.EventQuery;
import com.kodo.api.application.dto.EventResponse;
import com.kodo.api.application.dto.PagedResult;
import com.kodo.api.application.dto.requests.CreateEventRequest;
import com.kodo.api.application.ports.in.EventQueryService;
import com.kodo.api.application.ports.in.EventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final EventQueryService eventQueryService;

    public EventController(EventService eventService, EventQueryService eventQueryService) {
        this.eventService = eventService;
        this.eventQueryService = eventQueryService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<Void>> createEvent(
            @Valid @RequestBody CreateEventRequest request) {

        return eventService.createEvent(request)
                .thenApply(ignored -> ResponseEntity.accepted().build());

    }

    @GetMapping
    public ResponseEntity<PagedResult<EventResponse>> getEvents(
            @RequestParam(required = false) String gameId,
            @RequestParam(required = false) String playerId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        EventQuery query = new EventQuery(
                gameId,
                playerId,
                type,
                page,
                size
        );

        return ResponseEntity.ok(eventQueryService.findEvents(query));
    }
}
