package com.bookhub.repository;

import com.bookhub.dto.AdminQuestionRowDTO;
import com.bookhub.dto.PageResult;
import com.bookhub.entity.Answer;
import com.bookhub.entity.Question;
import com.bookhub.entity.QuestionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class QuestionRepository extends BaseRepository<Question, Long> {

    public QuestionRepository() {
        super(Question.class);
    }

    public Optional<Question> findByIdLocked(EntityManager em, Long questionId) {
        if (questionId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(em.find(Question.class, questionId, LockModeType.PESSIMISTIC_WRITE));
    }

    public Optional<Answer> findAnswerByQuestionId(EntityManager em, Long questionId) {
        List<Answer> rows = em.createQuery(
                        "SELECT a FROM Answer a JOIN FETCH a.responderUser WHERE a.question.questionId = :qid",
                        Answer.class)
                .setParameter("qid", questionId)
                .setMaxResults(1)
                .getResultList();
        return rows.stream().findFirst();
    }

    public long countByStatus(EntityManager em, QuestionStatus status) {
        if (status == null) {
            return countAll(em);
        }
        Long cnt = em.createQuery("SELECT COUNT(q) FROM Question q WHERE q.status = :st", Long.class)
                .setParameter("st", status)
                .getSingleResult();
        return cnt != null ? cnt : 0L;
    }

    public PageResult<AdminQuestionRowDTO> searchAdminQuestions(
            EntityManager em, QuestionStatus status, int page, int pageSize) {
        long total = countByStatus(em, status);
        if (total == 0L) {
            return new PageResult<>(Collections.emptyList(), 1, pageSize, 0L);
        }
        int totalPages = (int) Math.ceil((double) total / pageSize);
        int safePage = Math.min(Math.max(1, page), totalPages);
        String where = status == null ? " " : " WHERE q.status = :st ";
        TypedQuery<AdminQuestionRowDTO> query = em.createQuery(
                "SELECT new com.bookhub.dto.AdminQuestionRowDTO("
                        + "q.questionId, b.bookId, b.title, u.fullName, u.email, "
                        + "q.subject, q.content, q.status, q.createdAt, "
                        + "a.answerId, a.content, ru.fullName, a.createdAt) "
                        + "FROM Question q JOIN q.book b JOIN q.user u "
                        + "LEFT JOIN Answer a ON a.question = q "
                        + "LEFT JOIN a.responderUser ru" + where
                        + "ORDER BY q.createdAt DESC, q.questionId DESC",
                AdminQuestionRowDTO.class);
        if (status != null) {
            query.setParameter("st", status);
        }
        List<AdminQuestionRowDTO> items = query.setFirstResult((safePage - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
        return new PageResult<>(items, safePage, pageSize, total);
    }
}
