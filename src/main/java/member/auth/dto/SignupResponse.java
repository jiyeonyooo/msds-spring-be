package member.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// POST /api/auth/signup 응답 데이터. 안내 메시지는 ApiResponse.message로 내려간다.
@Getter
@AllArgsConstructor
public class SignupResponse {
    private String email;
}
