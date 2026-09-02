package com.kodo.api.application.dto;

import java.util.List;

public record PagedResult<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}