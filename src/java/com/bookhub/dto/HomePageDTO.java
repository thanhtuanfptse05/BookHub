package com.bookhub.dto;

import java.util.Collections;
import java.util.List;

public final class HomePageDTO {

    private final List<BookCardDTO> featuredBooks;
    private final List<BookCardDTO> newestBooks;
    private final List<BookCardDTO> bestsellerBooks;
    private final List<CategoryShortcutDTO> categories;

    public HomePageDTO(List<BookCardDTO> featuredBooks, List<BookCardDTO> newestBooks,
            List<BookCardDTO> bestsellerBooks, List<CategoryShortcutDTO> categories) {
        this.featuredBooks = featuredBooks == null ? Collections.emptyList() : Collections.unmodifiableList(featuredBooks);
        this.newestBooks = newestBooks == null ? Collections.emptyList() : Collections.unmodifiableList(newestBooks);
        this.bestsellerBooks = bestsellerBooks == null ? Collections.emptyList() : Collections.unmodifiableList(bestsellerBooks);
        this.categories = categories == null ? Collections.emptyList() : Collections.unmodifiableList(categories);
    }

    public List<BookCardDTO> getFeaturedBooks() { return featuredBooks; }
    public List<BookCardDTO> getNewestBooks() { return newestBooks; }
    public List<BookCardDTO> getBestsellerBooks() { return bestsellerBooks; }
    public List<CategoryShortcutDTO> getCategories() { return categories; }
}
