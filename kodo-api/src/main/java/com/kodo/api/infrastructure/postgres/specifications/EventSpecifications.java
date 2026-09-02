package com.kodo.api.infrastructure.postgres.specifications;

import com.kodo.api.application.dto.EventQuery;
import com.kodo.api.infrastructure.postgres.entities.EventEntity;
import org.springframework.data.jpa.domain.Specification;

public final class EventSpecifications {

    private EventSpecifications() {
    }

    public static Specification<EventEntity> from(EventQuery query) {
        Specification<EventEntity> specification =
                Specification.unrestricted();

        if (query.gameId() != null) {
            specification = specification.and(
                    (root, criteriaQuery, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("gameId"),
                                    query.gameId()
                            )
            );
        }

        if (query.playerId() != null) {
            specification = specification.and(
                    (root, criteriaQuery, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("playerId"),
                                    query.playerId()
                            )
            );
        }

        if (query.type() != null) {
            specification = specification.and(
                    (root, criteriaQuery, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("eventType"),
                                    query.type()
                            )
            );
        }

        return specification;
    }
}