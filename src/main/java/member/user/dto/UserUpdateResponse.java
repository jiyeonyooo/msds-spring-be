package member.user.dto;

import member.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

// PATCH /api/users/me 응답 바디.
@Getter
@AllArgsConstructor
public class UserUpdateResponse {
    private Long userId;
    private String email;
    private String name;
    private String phoneNumber;
    private LocalDateTime updatedAt;
    private String message;

    public static UserUpdateResponse of(User user, String message) {
        return new UserUpdateResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getUpdatedAt(),
                message
        );
    }
}