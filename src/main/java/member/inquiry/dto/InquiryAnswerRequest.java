package member.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryAnswerRequest {

    @NotBlank(message = "답변 내용은 필수 입력값입니다.")
    @Size(max = 2000, message = "답변 내용은 2000자를 초과할 수 없습니다.")
    private String answerContent;
}
