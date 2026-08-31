package member.inquiry.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import member.inquiry.domain.Inquiry;
import member.inquiry.domain.InquiryStatus;

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

    // 날짜 타입은 응답 JSON 포맷을 고정해 클라이언트가 파싱 규칙을 통일할 수 있게 한다
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime answeredAt;   // 답변 전이면 null

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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