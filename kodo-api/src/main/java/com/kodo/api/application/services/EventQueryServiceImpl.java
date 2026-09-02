package com.kodo.api.application.services;

import com.kodo.api.application.dto.EventQuery;
import com.kodo.api.application.dto.EventResponse;
import com.kodo.api.application.dto.PagedResult;
import com.kodo.api.application.ports.in.EventQueryService;
import com.kodo.api.application.ports.out.EventQueryRepository;
import org.springframework.stereotype.Service;

@Service
public class EventQueryServiceImpl implements EventQueryService {

    private final EventQueryRepository eventQueryRepository;

    public EventQueryServiceImpl(EventQueryRepository eventQueryRepository) {
        this.eventQueryRepository = eventQueryRepository;
    }

    @Override
    public PagedResult<EventResponse> findEvents(EventQuery query) {
        return eventQueryRepository.findEvents(query);
    }
}
