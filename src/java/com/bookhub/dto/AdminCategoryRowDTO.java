package com.bookhub.dto;

public final class AdminCategoryRowDTO {

    private final Integer categoryId;
    private final String name;
    private final String slug;
    private final String description;
    private final boolean active;
    private final long totalBooks;

    public AdminCategoryRowDTO(Integer categoryId, String name, String slug,
            String description, boolean active, long totalBooks) {
        this.categoryId = categoryId;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.active = active;
        this.totalBooks = totalBooks;
    }

    public Integer getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public long getTotalBooks() { return totalBooks; }
}
