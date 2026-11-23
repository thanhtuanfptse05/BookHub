package com.bookhub.service;

import com.bookhub.config.AppConstants;
import com.bookhub.config.JpaUtil;
import com.bookhub.dto.AdminReviewRowDTO;
import com.bookhub.dto.PageResult;
import com.bookhub.entity.Book;
import com.bookhub.entity.Review;
import com.bookhub.entity.ReviewStatus;
import com.bookhub.entity.User;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.repository.BookRepository;
import com.bookhub.repository.OrderRepository;
import com.bookhub.repository.ReviewRepository;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Verified-buyer review submission (PENDING) and Admin moderation service (FR-01..04).
 */
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final OrderRepository orderRepo;
    private final BookRepository bookRepo;

    public ReviewService() {
        this(new ReviewRepository(), new OrderRepository(), new BookRepository());
    }

    public ReviewService(ReviewRepository reviewRepo, OrderRepository orderRepo, BookRepository bookRepo) {
        this.reviewRepo = reviewRepo;
        this.orderRepo = orderRepo;
        this.bookRepo = bookRepo;
    }

    public Long submitReview(Long userId, Long bookId, int rating, String title, String content) {
        String cleanTitle = title == null ? "" : title.trim();
        String cleanContent = content == null ? "" : content.trim();

        if (rating < 1 || rating > 5) {
            throw new BusinessException("Số sao đánh giá phải nằm trong khoảng từ 1 đến 5.");
        }
        if (cleanTitle.length() > 150) {
            throw new BusinessException("Tiêu đề đánh giá tối đa 150 ký tự.");
        }
        if (cleanContent.isEmpty() || cleanContent.length() > 2000) {
            throw new BusinessException("Nội dung đánh giá là bắt buộc và tối đa 2.000 ký tự.");
        }

        return JpaUtil.executeInTransaction(em -> {
            Book book = bookRepo.findActiveDetailById(em, bookId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy sách đang kinh doanh."));
            if (!orderRepo.hasDeliveredPurchase(em, userId, bookId)) {
                throw new BusinessException("Chỉ khách hàng đã mua và nhận cuốn sách này (DELIVERED) mới được gửi đánh giá.");
            }
            if (reviewRepo.existsByUserAndBook(em, userId, bookId)) {
                throw new BusinessException("Bạn đã gửi đánh giá cho cuốn sách này trước đó.");
            }
            Review review = new Review();
            review.setBook(book);
            review.setUser(em.getReference(User.class, userId));
            review.setRating((byte) rating);
            review.setTitle(cleanTitle.isEmpty() ? null : cleanTitle);
            review.setContent(cleanContent);
            review.setStatus(ReviewStatus.PENDING);
            reviewRepo.persist(em, review);
            em.flush();
            return review.getReviewId();
        });
    }

    public PageResult<AdminReviewRowDTO> listAdminReviews(ReviewStatus status, int page) {
        return JpaUtil.executeReadOnly(em ->
                reviewRepo.searchAdminReviews(em, status, page, AppConstants.DEFAULT_ADMIN_PAGE_SIZE));
    }

    public Map<String, Long> countByStatus() {
        return JpaUtil.executeReadOnly(em -> {
            Map<String, Long> map = new LinkedHashMap<>();
            map.put("ALL", reviewRepo.countByStatus(em, null));
            map.put("PENDING", reviewRepo.countByStatus(em, ReviewStatus.PENDING));
            map.put("APPROVED", reviewRepo.countByStatus(em, ReviewStatus.APPROVED));
            map.put("HIDDEN", reviewRepo.countByStatus(em, ReviewStatus.HIDDEN));
            return map;
        });
    }

    public void moderateReview(Long reviewId, ReviewStatus targetStatus) {
        if (targetStatus != ReviewStatus.APPROVED && targetStatus != ReviewStatus.HIDDEN) {
            throw new BusinessException("Trạng thái kiểm duyệt đánh giá không hợp lệ.");
        }
        JpaUtil.executeInTransactionVoid(em -> {
            Review review = reviewRepo.findById(em, reviewId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy đánh giá."));
            review.setStatus(targetStatus);
        });
    }
}
