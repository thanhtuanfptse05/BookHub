package com.bookhub.dto;

/**
 * Category shortcut card DTO with active book count for SCR-01 and SCR-02 filter list.
 */
public final class CategoryShortcutDTO {

    private final Integer categoryId;
    private final String name;
    private final String slug;
    private final long activeBookCount;

    public CategoryShortcutDTO(Integer categoryId, String name, String slug, long activeBookCount) {
        this.categoryId = categoryId;
        this.name = name;
        this.slug = slug;
        this.activeBookCount = activeBookCount;
    }

    public Integer getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public long getActiveBookCount() { return activeBookCount; }
}
