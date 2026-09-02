package com.kodo.api.application.ports.in;

import com.kodo.api.application.dto.EventQuery;
import com.kodo.api.application.dto.EventResponse;
import com.kodo.api.application.dto.PagedResult;

public interface EventQueryService {

    PagedResult<EventResponse> findEvents(EventQuery query);
}