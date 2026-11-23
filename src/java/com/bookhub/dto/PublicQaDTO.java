package com.bookhub.dto;

import com.bookhub.util.FormatUtil;
import java.time.LocalDateTime;

public final class PublicQaDTO {

    private final Long questionId;
    private final String askerName;
    private final String subject;
    private final String content;
    private final String createdAtFormatted;
    private final String answerContent;
    private final String responderName;
    private final String answeredAtFormatted;

    public PublicQaDTO(Long questionId, String askerName, String subject, String content,
            LocalDateTime createdAt, String answerContent, String responderName, LocalDateTime answeredAt) {
        this.questionId = questionId;
        this.askerName = askerName;
        this.subject = subject;
        this.content = content;
        this.createdAtFormatted = FormatUtil.formatDateTime(createdAt);
        this.answerContent = answerContent;
        this.responderName = responderName;
        this.answeredAtFormatted = FormatUtil.formatDateTime(answeredAt);
    }

    public Long getQuestionId() { return questionId; }
    public String getAskerName() { return askerName; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }
    public String getCreatedAtFormatted() { return createdAtFormatted; }
    public String getAnswerContent() { return answerContent; }
    public String getResponderName() { return responderName; }
    public String getAnsweredAtFormatted() { return answeredAtFormatted; }
}
