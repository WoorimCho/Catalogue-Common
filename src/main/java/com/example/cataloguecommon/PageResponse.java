package com.example.cataloguecommon;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Stable page envelope for list endpoints.
 *
 * <p>Spring Data's {@code Page} serializes with its own internal shape
 * ({@code pageable}, a nested {@code sort} object, {@code number}, …) which is
 * not part of any contract and has changed between Spring versions. This record
 * is the shape clients actually depend on; controllers wrap their {@code Page}
 * in {@code PageResponse.of(page)} instead of returning it directly.
 *
 * @param page   zero-based index of this page
 * @param size   requested page size
 * @param numberOfElements items actually on this page
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        int numberOfElements,
        boolean empty
) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.getNumberOfElements(),
                page.isEmpty());
    }
}
