package com.bookhub.dto;

import java.util.Locale;

/**
 * Strict server-side allow-list for public catalog sorting (FR-05, EC-03, BR-CAT-004).
 */
public enum BookSortOption {
    NEWEST("newest", "Mới nhất"),
    PRICE_ASC("price_asc", "Giá tăng dần"),
    PRICE_DESC("price_desc", "Giá giảm dần"),
    TITLE_ASC("title_asc", "Tên A–Z"),
    BESTSELLER("bestseller", "Bán chạy nhất");

    private final String code;
    private final String label;

    BookSortOption(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static BookSortOption fromParam(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return NEWEST;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (BookSortOption opt : values()) {
            if (opt.code.equals(normalized)) {
                return opt;
            }
        }
        return NEWEST;
    }
}
