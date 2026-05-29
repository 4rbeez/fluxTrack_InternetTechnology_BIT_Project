package com.example.demo.controller;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Stable JSON shape for paginated REST responses.
 *
 * Wrapping Spring Data's Page&lt;T&gt; here (rather than returning it directly)
 * avoids depending on Spring's internal PageImpl serialization, which is
 * version-fragile and includes a lot of fields the frontend doesn't need.
 * The fields exposed here are exactly what products.js / orders.js consume:
 * the items themselves plus enough metadata to render pagination controls.
 */
public record PagedResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
}
