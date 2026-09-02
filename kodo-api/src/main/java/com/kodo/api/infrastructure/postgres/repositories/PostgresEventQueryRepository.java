package com.kodo.api.infrastructure.postgres.repositories;

import com.kodo.api.application.dto.EventQuery;
import com.kodo.api.application.dto.EventResponse;
import com.kodo.api.application.dto.PagedResult;
import com.kodo.api.application.ports.out.EventQueryRepository;
import com.kodo.api.infrastructure.postgres.entities.EventEntity;
import com.kodo.api.infrastructure.postgres.specifications.EventSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PostgresEventQueryRepository implements EventQueryRepository {

    private final JpaEventRepository jpaEventRepository;

    public PostgresEventQueryRepository(JpaEventRepository jpaEventRepository) {
        this.jpaEventRepository = jpaEventRepository;
    }

    @Override
    public PagedResult<EventResponse> findEvents(EventQuery query) {

        Pageable pageable = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(
                        Sort.Order.desc("occurredAt"),
                        Sort.Order.desc("id")
                )
        );

        Page<EventEntity> page = jpaEventRepository.findAll(
                EventSpecifications.from(query),
                pageable
        );

        List<EventResponse> items = page.getContent()
                .stream()
                .map(this::toResponse)
                .toList();


        return new PagedResult<>(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private EventResponse toResponse(EventEntity entity) {
        return new EventResponse(
                entity.getEventId(),
                entity.getGameId(),
                entity.getPlayerId(),
                entity.getEventType(),
                entity.getOccurredAt(),
                entity.getReceivedAt(),
                entity.getPayload()
        );
    }
}
