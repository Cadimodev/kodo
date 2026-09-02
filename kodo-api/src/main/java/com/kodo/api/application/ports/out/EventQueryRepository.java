package com.kodo.api.application.ports.out;

import com.kodo.api.application.dto.EventQuery;
import com.kodo.api.application.dto.EventResponse;
import com.kodo.api.application.dto.PagedResult;

public interface EventQueryRepository {

    PagedResult<EventResponse> findEvents(EventQuery query);
}