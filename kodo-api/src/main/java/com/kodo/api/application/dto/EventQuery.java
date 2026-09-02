package com.kodo.api.application.dto;

public record EventQuery(
        String gameId,
        String playerId,
        String type,
        int page,
        int size
) {
}