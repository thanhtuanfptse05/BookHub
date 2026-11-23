package com.bookhub.service;

import com.bookhub.config.AppConstants;
import com.bookhub.config.JpaUtil;
import com.bookhub.dto.BookCardDTO;
import com.bookhub.dto.BookDetailDTO;
import com.bookhub.dto.BookFilterCriteria;
import com.bookhub.dto.CategoryShortcutDTO;
import com.bookhub.dto.HomePageDTO;
import com.bookhub.dto.PageResult;
import com.bookhub.dto.PublicQaDTO;
import com.bookhub.dto.PublicReviewDTO;
import com.bookhub.entity.Book;
import com.bookhub.entity.OrderStatus;
import com.bookhub.entity.QuestionStatus;
import com.bookhub.entity.ReviewStatus;
import com.bookhub.exception.NotFoundException;
import com.bookhub.repository.BookRepository;
import com.bookhub.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * Public read-only storefront catalog service (04-feat-public-catalog).
 */
public class CatalogService {

    private static final int HOME_SHELF_LIMIT = 8;
    private static final int SIMILAR_BOOKS_LIMIT = 4;

    private final BookRepository bookRepo;
    private final CategoryRepository categoryRepo;

    public CatalogService() {
        this(new BookRepository(), new CategoryRepository());
    }

    public CatalogService(BookRepository bookRepo, CategoryRepository categoryRepo) {
        this.bookRepo = bookRepo;
        this.categoryRepo = categoryRepo;
    }

    public HomePageDTO getHomeData() {
        return JpaUtil.executeReadOnly(em -> new HomePageDTO(
                bookRepo.findFeaturedActive(em, HOME_SHELF_LIMIT),
                bookRepo.findNewestActive(em, HOME_SHELF_LIMIT),
                bookRepo.findBestsellersActive(em, HOME_SHELF_LIMIT),
                categoryRepo.findActiveShortcuts(em)));
    }

    public List<CategoryShortcutDTO> listActiveCategories() {
        return JpaUtil.executeReadOnly(categoryRepo::findActiveShortcuts);
    }

    public PageResult<BookCardDTO> searchCatalog(BookFilterCriteria criteria, int page) {
        return JpaUtil.executeReadOnly(em ->
                bookRepo.searchActiveBooks(em, criteria, page, AppConstants.DEFAULT_CATALOG_PAGE_SIZE));
    }

    public BookDetailDTO getBookDetail(Long bookId, Long currentUserId) {
        return JpaUtil.executeReadOnly(em -> {
            Book book = bookRepo.findActiveDetailById(em, bookId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy cuốn sách bạn yêu cầu."));

            List<BookCardDTO> similar = bookRepo.findSimilarActive(
                    em, book.getCategory().getCategoryId(), book.getBookId(), SIMILAR_BOOKS_LIMIT);
            List<PublicReviewDTO> reviews = loadApprovedReviews(em, bookId);
            List<PublicQaDTO> qaList = loadAnsweredQa(em, bookId);

            long reviewCount = reviews.size();
            double avgRating = reviewCount == 0 ? 0.0
                    : reviews.stream().mapToInt(PublicReviewDTO::getRating).average().orElse(0.0);

            boolean alreadyReviewed = false;
            boolean canWriteReview = false;
            if (currentUserId != null) {
                alreadyReviewed = hasUserReviewed(em, currentUserId, bookId);
                canWriteReview = !alreadyReviewed && hasDeliveredPurchase(em, currentUserId, bookId);
            }

            return new BookDetailDTO(
                    book.getBookId(), book.getIsbn(), book.getTitle(), book.getAuthor(),
                    book.getPublisher(), book.getPublishedYear(), book.getPrice(), book.getStockQty(),
                    book.getShortDescription(), book.getDescription(), book.getCoverUrl(),
                    book.getCategory().getCategoryId(), book.getCategory().getName(),
                    avgRating, reviewCount, similar, reviews, qaList, canWriteReview, alreadyReviewed);
        });
    }

    private static List<PublicReviewDTO> loadApprovedReviews(EntityManager em, Long bookId) {
        return em.createQuery(
                        "SELECT new com.bookhub.dto.PublicReviewDTO("
                                + "r.reviewId, u.fullName, CAST(r.rating AS integer), r.title, r.content, r.createdAt) "
                                + "FROM Review r JOIN r.user u "
                                + "WHERE r.book.bookId = :bid AND r.status = :approved "
                                + "ORDER BY r.createdAt DESC",
                        PublicReviewDTO.class)
                .setParameter("bid", bookId)
                .setParameter("approved", ReviewStatus.APPROVED)
                .getResultList();
    }

    private static List<PublicQaDTO> loadAnsweredQa(EntityManager em, Long bookId) {
        return em.createQuery(
                        "SELECT new com.bookhub.dto.PublicQaDTO("
                                + "q.questionId, qu.fullName, q.subject, q.content, q.createdAt, "
                                + "a.content, ru.fullName, a.createdAt) "
                                + "FROM Question q JOIN q.user qu "
                                + "JOIN Answer a ON a.question = q JOIN a.responderUser ru "
                                + "WHERE q.book.bookId = :bid AND q.status = :answered "
                                + "ORDER BY q.createdAt DESC",
                        PublicQaDTO.class)
                .setParameter("bid", bookId)
                .setParameter("answered", QuestionStatus.ANSWERED)
                .getResultList();
    }

    private static boolean hasUserReviewed(EntityManager em, Long userId, Long bookId) {
        Long cnt = em.createQuery(
                        "SELECT COUNT(r) FROM Review r WHERE r.user.userId = :uid AND r.book.bookId = :bid",
                        Long.class)
                .setParameter("uid", userId)
                .setParameter("bid", bookId)
                .getSingleResult();
        return cnt != null && cnt > 0L;
    }

    private static boolean hasDeliveredPurchase(EntityManager em, Long userId, Long bookId) {
        Long cnt = em.createQuery(
                        "SELECT COUNT(oi) FROM OrderItem oi JOIN oi.order o "
                                + "WHERE o.user.userId = :uid AND oi.book.bookId = :bid AND o.status = :delivered",
                        Long.class)
                .setParameter("uid", userId)
                .setParameter("bid", bookId)
                .setParameter("delivered", OrderStatus.DELIVERED)
                .getSingleResult();
        return cnt != null && cnt > 0L;
    }
}
