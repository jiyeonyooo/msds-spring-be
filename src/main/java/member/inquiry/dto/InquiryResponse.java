package member.inquiry.dto;

import member.inquiry.domain.Inquiry;
import member.inquiry.domain.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// 목록/상세 조회에 공통으로 사용하는 응답 DTO
@Getter
@AllArgsConstructor
public class InquiryResponse {
    private Long inquiryId;
    private String authorEmail;
    private String title;
    private String content;
    private InquiryStatus status;
    private String answerContent;   // 답변 전이면 null
    private LocalDateTime answeredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InquiryResponse from(Inquiry inquiry) {
        return new InquiryResponse(
                inquiry.getId(),
                inquiry.getUser().getEmail(),
                inquiry.getTitle(),
                inquiry.getContent(),
                inquiry.getStatus(),
                inquiry.getAnswerContent(),
                inquiry.getAnsweredAt(),
                inquiry.getCreatedAt(),
                inquiry.getUpdatedAt()
        );
    }
}