package member.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

// DELETE /api/users/me 요청 바디

@Getter
@NoArgsConstructor
public class UserDeleteRequest {
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;
}