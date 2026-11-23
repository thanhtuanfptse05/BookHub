package com.bookhub.dto;

import com.bookhub.entity.QuestionStatus;
import com.bookhub.util.FormatUtil;
import java.time.LocalDateTime;

public final class AdminQuestionRowDTO {

    private final Long questionId;
    private final Long bookId;
    private final String bookTitle;
    private final String askerName;
    private final String askerEmail;
    private final String subject;
    private final String content;
    private final QuestionStatus status;
    private final String createdAtFormatted;
    private final Long answerId;
    private final String answerContent;
    private final String responderName;
    private final String answeredAtFormatted;

    public AdminQuestionRowDTO(Long questionId, Long bookId, String bookTitle, String askerName,
            String askerEmail, String subject, String content, QuestionStatus status,
            LocalDateTime createdAt, Long answerId, String answerContent,
            String responderName, LocalDateTime answeredAt) {
        this.questionId = questionId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.askerName = askerName;
        this.askerEmail = askerEmail;
        this.subject = subject;
        this.content = content;
        this.status = status;
        this.createdAtFormatted = FormatUtil.formatDateTime(createdAt);
        this.answerId = answerId;
        this.answerContent = answerContent;
        this.responderName = responderName;
        this.answeredAtFormatted = FormatUtil.formatDateTime(answeredAt);
    }

    public Long getQuestionId() { return questionId; }
    public Long getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public String getAskerName() { return askerName; }
    public String getAskerEmail() { return askerEmail; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }
    public QuestionStatus getStatus() { return status; }
    public String getCreatedAtFormatted() { return createdAtFormatted; }
    public Long getAnswerId() { return answerId; }
    public boolean isAnswered() { return answerId != null; }
    public String getAnswerContent() { return answerContent; }
    public String getResponderName() { return responderName; }
    public String getAnsweredAtFormatted() { return answeredAtFormatted; }
}
