package com.bookhub.service;

import com.bookhub.config.JpaUtil;
import com.bookhub.dto.BookDetailDTO;
import com.bookhub.dto.CheckoutRequest;
import com.bookhub.entity.Book;
import com.bookhub.entity.OrderStatus;
import com.bookhub.entity.Question;
import com.bookhub.entity.QuestionStatus;
import com.bookhub.entity.Review;
import com.bookhub.entity.ReviewStatus;
import com.bookhub.exception.AccessDeniedException;
import com.bookhub.exception.BusinessException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for 07-feat-reviews-qa (AC-01..AC-08) against live SQL Server BookHubDB.
 */
class ReviewsQaIntegrationTest {

    private final AuthService authService = new AuthService();
    private final CartService cartService = new CartService();
    private final CheckoutService checkoutService = new CheckoutService();
    private final OrderService orderService = new OrderService();
    private final ReviewService reviewService = new ReviewService();
    private final QuestionService questionService = new QuestionService();
    private final CatalogService catalogService = new CatalogService();

    @BeforeAll
    static void init() {
        JpaUtil.init();
    }

    @AfterAll
    static void close() {
        JpaUtil.shutdown();
    }

    // @spec reviews-qa/AC-01
    // @spec reviews-qa/AC-02
    // @spec reviews-qa/AC-03
    @Test
    void verifiedBuyerReviewsRequireDeliveredOrderModerateApproveAndHideAndRejectDuplicates() {
        String marker = UUID.randomUUID().toString().substring(0, 8);
        Long buyerId = null;
        Long tempBookId = null;

        try {
            buyerId = authService.register("Reviewer " + marker, "rev." + marker + "@bookhub.vn", null, "Pass1234", "Pass1234").getUserId();
            tempBookId = createTempBook("Review Book " + marker);

            Long uid = buyerId;
            Long bid = tempBookId;

            // AC-02: cannot review without purchase
            assertThrows(BusinessException.class, () -> reviewService.submitReview(uid, bid, 5, "Tốt", "Chưa mua"));

            // Purchase -> PENDING/SHIPPING still cannot review
            cartService.addToCart(buyerId, tempBookId, 1);
            Long orderId = checkoutService.placeCodOrder(buyerId, new CheckoutRequest("Reviewer", "0912345678", "Hà Nội", null));
            orderService.transitionByAdmin(1L, orderId, OrderStatus.CONFIRMED);
            orderService.transitionByAdmin(1L, orderId, OrderStatus.SHIPPING);
            assertThrows(BusinessException.class, () -> reviewService.submitReview(uid, bid, 5, "Tốt", "Đang giao"));

            // Deliver order -> now eligible
            orderService.transitionByAdmin(1L, orderId, OrderStatus.DELIVERED);

            // AC-02: invalid rating 0 or 6 rejected
            assertThrows(BusinessException.class, () -> reviewService.submitReview(uid, bid, 0, "Bad", "Invalid"));
            assertThrows(BusinessException.class, () -> reviewService.submitReview(uid, bid, 6, "Bad", "Invalid"));

            // AC-01: valid review created as PENDING, hidden from public rating until APPROVED
            Long reviewId = reviewService.submitReview(buyerId, tempBookId, 5, "Sách hay", "<script>alert(1)</script> Nội dung chi tiết");
            Review saved = JpaUtil.executeReadOnly(em -> em.find(Review.class, reviewId));
            assertEquals(ReviewStatus.PENDING, saved.getStatus());
            assertEquals(0L, catalogService.getBookDetail(tempBookId, null).getReviewCount());

            // AC-02: duplicate review by same user on same book rejected
            assertThrows(BusinessException.class, () -> reviewService.submitReview(uid, bid, 4, "Lần 2", "Trùng"));

            // AC-03: approve -> appears publicly & computes average rating; hide -> disappears again
            reviewService.moderateReview(reviewId, ReviewStatus.APPROVED);
            BookDetailDTO afterApprove = catalogService.getBookDetail(tempBookId, null);
            assertEquals(1L, afterApprove.getReviewCount());
            assertEquals(5.0, afterApprove.getAverageRating(), 0.01);

            reviewService.moderateReview(reviewId, ReviewStatus.HIDDEN);
            BookDetailDTO afterHide = catalogService.getBookDetail(tempBookId, null);
            assertEquals(0L, afterHide.getReviewCount());
            assertEquals(0.0, afterHide.getAverageRating(), 0.01);
        } finally {
            cleanup(buyerId, tempBookId);
        }
    }

    // @spec reviews-qa/AC-04
    // @spec reviews-qa/AC-05
    // @spec reviews-qa/AC-06
    // @spec reviews-qa/AC-07
    // @spec reviews-qa/AC-08
    @Test
    void qaCustomerAskAdminSingleAnswerUpdateHideAndJspXssEscaping() throws IOException {
        String marker = UUID.randomUUID().toString().substring(0, 8);
        Long customerId = null;
        Long tempBookId = null;

        try {
            customerId = authService.register("Asker " + marker, "qa." + marker + "@bookhub.vn", null, "Pass1234", "Pass1234").getUserId();
            tempBookId = createTempBook("QA Book " + marker);
            Long uid = customerId;
            Long bid = tempBookId;

            // AC-04: invalid blank subject rejected; valid question created as OPEN
            assertThrows(BusinessException.class, () -> questionService.askQuestion(uid, bid, "   ", "Nội dung"));
            Long qId = questionService.askQuestion(customerId, tempBookId, "Hỏi về bìa <script>alert(1)</script>", "Có bọc plastic không?");
            Question q = JpaUtil.executeReadOnly(em -> em.find(Question.class, qId));
            assertEquals(QuestionStatus.OPEN, q.getStatus());

            // AC-05: non-Admin blocked; Admin answers atomically -> creates 1 Answer + sets Question ANSWERED
            assertThrows(AccessDeniedException.class, () -> questionService.answerQuestion(uid, qId, "Customer fake reply"));
            questionService.answerQuestion(1L, qId, "Sách có màng co chống bụi ạ.");
            BookDetailDTO afterAnswer = catalogService.getBookDetail(tempBookId, null);
            assertEquals(1, afterAnswer.getAnsweredQuestions().size());
            assertEquals("Sách có màng co chống bụi ạ.", afterAnswer.getAnsweredQuestions().get(0).getAnswerContent());

            // AC-06: second action=answer rejected; Gate 1 Q1 updateAnswer edits the single existing Answer cleanly
            assertThrows(BusinessException.class, () -> questionService.answerQuestion(1L, qId, "Duplicate answer"));
            questionService.updateAnswer(1L, qId, "Sách có màng co và bookmark kèm theo.");
            assertEquals("Sách có màng co và bookmark kèm theo.",
                    catalogService.getBookDetail(tempBookId, null).getAnsweredQuestions().get(0).getAnswerContent());

            // AC-07: hide removes question from public detail without hard-delete
            questionService.hideQuestion(qId);
            assertTrue(catalogService.getBookDetail(tempBookId, null).getAnsweredQuestions().isEmpty());
            assertEquals(QuestionStatus.HIDDEN, JpaUtil.executeReadOnly(em -> em.find(Question.class, qId).getStatus()));

            // AC-08: verify all 3 views render user review/Q&A text strictly via <c:out> (no raw unescaped EL)
            for (String viewPath : new String[]{
                    "web/WEB-INF/views/site/book-detail.jsp",
                    "web/WEB-INF/views/admin/reviews.jsp",
                    "web/WEB-INF/views/admin/questions.jsp"}) {
                String jsp = Files.readString(Paths.get(viewPath), StandardCharsets.UTF_8);
                assertTrue(jsp.contains("<c:out"), "Expected <c:out> escaping in " + viewPath);
                assertFalse(jsp.contains("escapeXml=\"false\""), "Forbidden escapeXml=false in " + viewPath);
            }
        } finally {
            cleanup(customerId, tempBookId);
        }
    }

    private Long createTempBook(String title) {
        return JpaUtil.executeInTransaction(em -> {
            com.bookhub.entity.Category cat = em.createQuery(
                    "SELECT c FROM Category c WHERE c.active = true", com.bookhub.entity.Category.class)
                    .setMaxResults(1).getSingleResult();
            Book b = new Book();
            b.setCategory(cat);
            b.setTitle(title);
            b.setAuthor("QA Author");
            b.setPrice(new BigDecimal("120000.00"));
            b.setStockQty(5);
            b.setActive(true);
            em.persist(b);
            em.flush();
            return b.getBookId();
        });
    }

    private void cleanup(Long userId, Long bookId) {
        JpaUtil.executeInTransactionVoid(em -> {
            if (bookId != null) {
                em.createQuery("DELETE FROM Answer a WHERE a.question.questionId IN (SELECT q.questionId FROM Question q WHERE q.book.bookId = :b)")
                        .setParameter("b", bookId).executeUpdate();
                em.createQuery("DELETE FROM Question q WHERE q.book.bookId = :b").setParameter("b", bookId).executeUpdate();
                em.createQuery("DELETE FROM Review r WHERE r.book.bookId = :b").setParameter("b", bookId).executeUpdate();
            }
            if (userId != null) {
                em.createQuery("DELETE FROM OrderItem oi WHERE oi.order.orderId IN (SELECT o.orderId FROM Order o WHERE o.user.userId = :u)")
                        .setParameter("u", userId).executeUpdate();
                em.createQuery("DELETE FROM Order o WHERE o.user.userId = :u").setParameter("u", userId).executeUpdate();
                em.createQuery("DELETE FROM CartItem ci WHERE ci.cart.cartId IN (SELECT c.cartId FROM Cart c WHERE c.user.userId = :u)")
                        .setParameter("u", userId).executeUpdate();
                em.createQuery("DELETE FROM Cart c WHERE c.user.userId = :u").setParameter("u", userId).executeUpdate();
            }
            if (bookId != null) {
                em.createQuery("DELETE FROM Book b WHERE b.bookId = :b").setParameter("b", bookId).executeUpdate();
            }
            if (userId != null) {
                em.createQuery("DELETE FROM User u WHERE u.userId = :u").setParameter("u", userId).executeUpdate();
            }
        });
    }
}
