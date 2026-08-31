package member.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import member.user.domain.User;

import java.time.LocalDateTime;

// PATCH /api/users/me 응답 데이터. 안내 메시지는 ApiResponse.message로 내려간다.
@Getter
@AllArgsConstructor
public class UserUpdateResponse {
    private Long userId;
    private String email;
    private String name;
    private String phoneNumber;

    // DB(LocalDateTime) 값을 클라이언트에 항상 같은 문자열 포맷으로 내려주기 위한 설정
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public static UserUpdateResponse from(User user) {
        return new UserUpdateResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getUpdatedAt()
        );
    }
}
