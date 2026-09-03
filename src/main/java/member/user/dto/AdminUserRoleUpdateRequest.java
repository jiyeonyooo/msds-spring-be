package member.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

// PATCH /api/admin/users/{userId}/role 요청 바디. 허용 값 검증은 서비스에서 한 번 더 확인한다.
@Getter
@NoArgsConstructor
public class AdminUserRoleUpdateRequest {

    @NotBlank(message = "권한은 필수 입력값입니다.")
    private String role;
}
