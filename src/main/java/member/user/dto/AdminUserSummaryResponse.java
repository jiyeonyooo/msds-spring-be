package member.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import member.user.domain.User;

import java.time.LocalDateTime;

public record AdminUserSummaryResponse(
        Long userId,
        String email,
        String name,
        String phoneNumber,
        String role,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt
) {
    public static AdminUserSummaryResponse from(User user) {
        return new AdminUserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
