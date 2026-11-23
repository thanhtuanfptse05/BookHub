package com.bookhub.dto;

import java.util.Collections;
import java.util.List;

/**
 * Generic database-paginated result wrapper (A-09, shared_context.md).
 */
public final class PageResult<T> {

    private final List<T> items;
    private final int page;
    private final int pageSize;
    private final long totalItems;
    private final int totalPages;

    public PageResult(List<T> items, int page, int pageSize, long totalItems) {
        this.items = items == null ? Collections.emptyList() : Collections.unmodifiableList(items);
        this.pageSize = Math.max(1, pageSize);
        this.totalItems = Math.max(0L, totalItems);
        this.totalPages = this.totalItems == 0L ? 0 : (int) Math.ceil((double) this.totalItems / this.pageSize);
        this.page = this.totalPages == 0 ? 1 : Math.min(Math.max(1, page), this.totalPages);
    }

    public List<T> getItems() { return items; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public long getTotalItems() { return totalItems; }
    public int getTotalPages() { return totalPages; }
    public boolean isEmpty() { return items.isEmpty(); }
}
