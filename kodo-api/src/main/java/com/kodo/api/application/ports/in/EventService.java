package com.kodo.api.application.ports.in;

import com.kodo.api.application.dto.requests.CreateEventRequest;

import java.util.concurrent.CompletableFuture;

public interface EventService {

    CompletableFuture<Void> createEvent(CreateEventRequest request);
}
