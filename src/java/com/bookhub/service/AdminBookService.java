package com.bookhub.service;

import com.bookhub.config.AppConstants;
import com.bookhub.config.JpaUtil;
import com.bookhub.dto.AdminBookRowDTO;
import com.bookhub.dto.BookFormDTO;
import com.bookhub.dto.PageResult;
import com.bookhub.entity.Book;
import com.bookhub.entity.Category;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.repository.BookRepository;
import com.bookhub.repository.CategoryRepository;
import com.bookhub.util.UrlSafeUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin CRUD and soft-deactivate service for dbo.Books (05-feat-admin-catalog-crud, FR-05..08).
 */
public class AdminBookService {

    private final BookRepository bookRepo;
    private final CategoryRepository categoryRepo;

    public AdminBookService() {
        this(new BookRepository(), new CategoryRepository());
    }

    public AdminBookService(BookRepository bookRepo, CategoryRepository categoryRepo) {
        this.bookRepo = bookRepo;
        this.categoryRepo = categoryRepo;
    }

    public PageResult<AdminBookRowDTO> searchAdminBooks(
            String keyword, Integer categoryId, String statusFilter, int page) {
        Boolean activeFilter = null;
        if ("active".equalsIgnoreCase(statusFilter)) {
            activeFilter = Boolean.TRUE;
        } else if ("inactive".equalsIgnoreCase(statusFilter)) {
            activeFilter = Boolean.FALSE;
        }
        Boolean finalActive = activeFilter;

        return JpaUtil.executeReadOnly(em -> {
            PageResult<Book> raw = bookRepo.searchAdminBooks(
                    em, keyword, categoryId, finalActive, page, AppConstants.DEFAULT_ADMIN_PAGE_SIZE);
            List<AdminBookRowDTO> rows = raw.getItems().stream()
                    .map(b -> new AdminBookRowDTO(
                            b.getBookId(), b.getIsbn(), b.getTitle(), b.getAuthor(),
                            b.getCategory().getName(), b.getPrice(), b.getStockQty(),
                            b.getCoverUrl(), b.isFeatured(), b.isActive()))
                    .collect(Collectors.toList());
            return new PageResult<>(rows, raw.getPage(), raw.getPageSize(), raw.getTotalItems());
        });
    }

    public BookFormDTO getForEdit(Long bookId) {
        return JpaUtil.executeReadOnly(em -> {
            Book b = bookRepo.findByIdWithCategory(em, bookId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy sách."));
            BookFormDTO dto = new BookFormDTO();
            dto.setBookId(b.getBookId());
            dto.setCategoryId(b.getCategory().getCategoryId());
            dto.setIsbn(b.getIsbn());
            dto.setTitle(b.getTitle());
            dto.setAuthor(b.getAuthor());
            dto.setPublisher(b.getPublisher());
            dto.setPublishedYear(b.getPublishedYear() != null ? String.valueOf(b.getPublishedYear()) : "");
            dto.setPrice(b.getPrice().setScale(0, RoundingMode.HALF_UP).toPlainString());
            dto.setStockQty(String.valueOf(b.getStockQty()));
            dto.setShortDescription(b.getShortDescription());
            dto.setDescription(b.getDescription());
            dto.setCoverUrl(b.getCoverUrl());
            dto.setFeatured(b.isFeatured());
            dto.setActive(b.isActive());
            return dto;
        });
    }

    public List<Category> listCategoriesForForm(Integer currentCategoryId) {
        return JpaUtil.executeReadOnly(em -> {
            List<Category> active = new ArrayList<>(categoryRepo.findAllActive(em));
            if (currentCategoryId != null && active.stream().noneMatch(c -> c.getCategoryId().equals(currentCategoryId))) {
                categoryRepo.findById(em, currentCategoryId).ifPresent(active::add);
            }
            return active;
        });
    }

    public Long saveBook(BookFormDTO form) {
        String title = trim(form.getTitle());
        String author = trim(form.getAuthor());
        String publisher = trim(form.getPublisher());
        String isbn = trim(form.getIsbn());
        String normalizedIsbn = isbn.isEmpty() ? null : isbn;
        String shortDesc = trim(form.getShortDescription());
        String desc = trim(form.getDescription());
        String coverUrl = trim(form.getCoverUrl());

        Map<String, String> errors = new LinkedHashMap<>();
        if (title.isEmpty() || title.length() > 200) {
            errors.put("title", "Tên sách là bắt buộc (tối đa 200 ký tự).");
        }
        if (author.isEmpty() || author.length() > 150) {
            errors.put("author", "Tác giả là bắt buộc (tối đa 150 ký tự).");
        }
        if (form.getCategoryId() == null) {
            errors.put("categoryId", "Vui lòng chọn danh mục sách.");
        }
        if (normalizedIsbn != null && normalizedIsbn.length() > 20) {
            errors.put("isbn", "ISBN tối đa 20 ký tự.");
        }
        if (publisher.length() > 150) {
            errors.put("publisher", "Nhà xuất bản tối đa 150 ký tự.");
        }
        if (shortDesc.length() > 500) {
            errors.put("shortDescription", "Mô tả ngắn tối đa 500 ký tự.");
        }
        if (coverUrl.length() > 500 || !UrlSafeUtil.isSafeCoverUrl(coverUrl)) {
            errors.put("coverUrl", "URL ảnh bìa không hợp lệ (chỉ chấp nhận http://, https:// hoặc /assets/...).");
        }

        Short yearVal = null;
        String rawYear = trim(form.getPublishedYear());
        if (!rawYear.isEmpty()) {
            try {
                int y = Integer.parseInt(rawYear);
                if (y < 1000 || y > 2200) {
                    errors.put("publishedYear", "Năm xuất bản phải nằm trong khoảng 1000–2200.");
                } else {
                    yearVal = (short) y;
                }
            } catch (NumberFormatException ex) {
                errors.put("publishedYear", "Năm xuất bản phải là số nguyên hợp lệ.");
            }
        }

        BigDecimal priceVal = null;
        try {
            priceVal = new BigDecimal(trim(form.getPrice())).setScale(2, RoundingMode.HALF_UP);
            if (priceVal.compareTo(BigDecimal.ZERO) <= 0) {
                errors.put("price", "Giá sách phải lớn hơn 0.");
            }
        } catch (NumberFormatException ex) {
            errors.put("price", "Giá sách phải là số hợp lệ > 0.");
        }

        int stockVal = 0;
        try {
            stockVal = Integer.parseInt(trim(form.getStockQty()));
            if (stockVal < 0) {
                errors.put("stockQty", "Tồn kho phải lớn hơn hoặc bằng 0.");
            }
        } catch (NumberFormatException ex) {
            errors.put("stockQty", "Tồn kho phải là số nguyên >= 0.");
        }

        if (!errors.isEmpty()) {
            throw new BusinessException("Vui lòng kiểm tra lại thông tin sách.", errors);
        }

        final Short finalYear = yearVal;
        final BigDecimal finalPrice = priceVal;
        final int finalStock = stockVal;

        return JpaUtil.executeInTransaction(em -> {
            Book existing = null;
            if (form.getBookId() != null) {
                existing = bookRepo.findByIdWithCategory(em, form.getBookId())
                        .orElseThrow(() -> new NotFoundException("Không tìm thấy sách cần cập nhật."));
            }

            Category targetCategory = categoryRepo.findById(em, form.getCategoryId()).orElse(null);
            boolean keepingLegacyInactive = existing != null
                    && targetCategory != null
                    && existing.getCategory().getCategoryId().equals(targetCategory.getCategoryId());
            if (targetCategory == null || (!targetCategory.isActive() && !keepingLegacyInactive)) {
                Map<String, String> catErr = new LinkedHashMap<>();
                catErr.put("categoryId", "Vui lòng chọn một danh mục đang hoạt động.");
                throw new BusinessException("Danh mục không hợp lệ hoặc đã ngừng hoạt động.", catErr);
            }

            if (normalizedIsbn != null && bookRepo.existsByIsbnExcludingId(em, normalizedIsbn, form.getBookId())) {
                Map<String, String> isbnErr = new LinkedHashMap<>();
                isbnErr.put("isbn", "ISBN này đã được dùng cho cuốn sách khác.");
                throw new BusinessException("ISBN đã tồn tại.", isbnErr);
            }

            Book book = existing != null ? existing : new Book();
            book.setCategory(targetCategory);
            book.setIsbn(normalizedIsbn);
            book.setTitle(title);
            book.setAuthor(author);
            book.setPublisher(publisher.isEmpty() ? null : publisher);
            book.setPublishedYear(finalYear);
            book.setPrice(finalPrice);
            book.setStockQty(finalStock);
            book.setShortDescription(shortDesc.isEmpty() ? null : shortDesc);
            book.setDescription(desc.isEmpty() ? null : desc);
            book.setCoverUrl(coverUrl.isEmpty() ? "/assets/images/book-placeholder.svg" : coverUrl);
            book.setFeatured(form.isFeatured());
            book.setActive(form.isActive());

            if (existing == null) {
                bookRepo.persist(em, book);
                em.flush();
            }
            return book.getBookId();
        });
    }

    public boolean toggleActive(Long bookId) {
        return JpaUtil.executeInTransaction(em -> {
            Book b = bookRepo.findById(em, bookId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy sách."));
            b.setActive(!b.isActive());
            return b.isActive();
        });
    }

    private static String trim(String v) {
        return v == null ? "" : v.trim();
    }
}
