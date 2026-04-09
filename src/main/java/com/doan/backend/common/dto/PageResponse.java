package com.doan.backend.common.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
public class PageResponse<T> {
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final List<T> items;

    public static <T, R> PageResponse<R> from(Page<T> source, List<R> items) {
        return PageResponse.<R>builder()
                .page(source.getNumber())
                .size(source.getSize())
                .totalElements(source.getTotalElements())
                .totalPages(source.getTotalPages())
                .items(items)
                .build();
    }
}
