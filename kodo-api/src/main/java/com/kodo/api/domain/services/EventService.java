package com.kodo.api.domain.services;

import com.kodo.api.domain.dto.requests.CreateEventRequest;

import java.util.concurrent.CompletableFuture;

public interface EventService {

    CompletableFuture<Void> createEvent(CreateEventRequest request);
}
