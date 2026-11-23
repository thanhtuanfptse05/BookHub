package com.bookhub.service;

import com.bookhub.config.AppConstants;
import com.bookhub.config.JpaUtil;
import com.bookhub.dto.AdminQuestionRowDTO;
import com.bookhub.dto.PageResult;
import com.bookhub.entity.Answer;
import com.bookhub.entity.Book;
import com.bookhub.entity.Question;
import com.bookhub.entity.QuestionStatus;
import com.bookhub.entity.RoleCode;
import com.bookhub.entity.User;
import com.bookhub.exception.AccessDeniedException;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.repository.BookRepository;
import com.bookhub.repository.QuestionRepository;
import com.bookhub.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Product Q&A service: Customer asks (OPEN); Admin answers once (atomic TX) or updates single Answer (FR-05..09, ADR-006).
 */
public class QuestionService {

    private final QuestionRepository questionRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;

    public QuestionService() {
        this(new QuestionRepository(), new BookRepository(), new UserRepository());
    }

    public QuestionService(QuestionRepository questionRepo, BookRepository bookRepo, UserRepository userRepo) {
        this.questionRepo = questionRepo;
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
    }

    public Long askQuestion(Long userId, Long bookId, String subject, String content) {
        String cleanSubject = subject == null ? "" : subject.trim();
        String cleanContent = content == null ? "" : content.trim();

        if (cleanSubject.isEmpty() || cleanSubject.length() > 150) {
            throw new BusinessException("Chủ đề câu hỏi là bắt buộc (tối đa 150 ký tự).");
        }
        if (cleanContent.isEmpty() || cleanContent.length() > 1500) {
            throw new BusinessException("Nội dung câu hỏi là bắt buộc (tối đa 1.500 ký tự).");
        }

        return JpaUtil.executeInTransaction(em -> {
            Book book = bookRepo.findActiveDetailById(em, bookId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy sách đang kinh doanh."));
            Question q = new Question();
            q.setBook(book);
            q.setUser(em.getReference(User.class, userId));
            q.setSubject(cleanSubject);
            q.setContent(cleanContent);
            q.setStatus(QuestionStatus.OPEN);
            questionRepo.persist(em, q);
            em.flush();
            return q.getQuestionId();
        });
    }

    public PageResult<AdminQuestionRowDTO> listAdminQuestions(QuestionStatus status, int page) {
        return JpaUtil.executeReadOnly(em ->
                questionRepo.searchAdminQuestions(em, status, page, AppConstants.DEFAULT_ADMIN_PAGE_SIZE));
    }

    public Map<String, Long> countByStatus() {
        return JpaUtil.executeReadOnly(em -> {
            Map<String, Long> map = new LinkedHashMap<>();
            map.put("ALL", questionRepo.countByStatus(em, null));
            map.put("OPEN", questionRepo.countByStatus(em, QuestionStatus.OPEN));
            map.put("ANSWERED", questionRepo.countByStatus(em, QuestionStatus.ANSWERED));
            map.put("HIDDEN", questionRepo.countByStatus(em, QuestionStatus.HIDDEN));
            return map;
        });
    }

    public Long answerQuestion(Long adminUserId, Long questionId, String content) {
        String cleanContent = validateAnswerContent(content);
        return JpaUtil.executeInTransaction(em -> {
            User responder = requireAdmin(em, adminUserId);
            Question question = questionRepo.findByIdLocked(em, questionId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy câu hỏi."));
            if (questionRepo.findAnswerByQuestionId(em, questionId).isPresent()) {
                throw new BusinessException("Câu hỏi này đã có câu trả lời. Không thể tạo thêm câu trả lời thứ hai.");
            }
            Answer answer = new Answer();
            answer.setQuestion(question);
            answer.setResponderUser(responder);
            answer.setContent(cleanContent);
            em.persist(answer);
            question.setStatus(QuestionStatus.ANSWERED);
            em.flush();
            return answer.getAnswerId();
        });
    }

    /**
     * Gate 1 Q1 (FR-08): updates the single existing Answer row without creating a 2nd record.
     */
    public void updateAnswer(Long adminUserId, Long questionId, String content) {
        String cleanContent = validateAnswerContent(content);
        JpaUtil.executeInTransactionVoid(em -> {
            User responder = requireAdmin(em, adminUserId);
            Answer existing = questionRepo.findAnswerByQuestionId(em, questionId)
                    .orElseThrow(() -> new BusinessException("Câu hỏi này chưa có phản hồi để cập nhật."));
            existing.setContent(cleanContent);
            existing.setResponderUser(responder);
        });
    }

    public void hideQuestion(Long questionId) {
        JpaUtil.executeInTransactionVoid(em -> {
            Question question = questionRepo.findByIdLocked(em, questionId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy câu hỏi."));
            question.setStatus(QuestionStatus.HIDDEN);
        });
    }

    private User requireAdmin(jakarta.persistence.EntityManager em, Long userId) {
        User user = userRepo.findById(em, userId)
                .orElseThrow(() -> new AccessDeniedException("Không có quyền quản trị."));
        if (user.getRole().getRoleCode() != RoleCode.ADMIN) {
            throw new AccessDeniedException("Chỉ Quản trị viên mới được phép trả lời câu hỏi khách hàng.");
        }
        return user;
    }

    private static String validateAnswerContent(String content) {
        String clean = content == null ? "" : content.trim();
        if (clean.isEmpty() || clean.length() > 2000) {
            throw new BusinessException("Nội dung phản hồi là bắt buộc (tối đa 2.000 ký tự).");
        }
        return clean;
    }
}
