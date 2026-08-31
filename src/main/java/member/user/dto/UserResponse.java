package member.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import member.user.domain.User;

import java.time.LocalDateTime;

// GET /api/users/me 응답 바디.
@Getter
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String email;
    private String name;
    private String phoneNumber;
    private String role;

    // 날짜 타입은 응답 JSON 포맷을 고정해 클라이언트가 파싱 규칙을 통일할 수 있게 한다
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}