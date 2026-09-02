package member.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import member.user.domain.User;

import java.time.LocalDateTime;

public record AdminUserDetailResponse(
        Long userId,
        String email,
        String name,
        String phoneNumber,
        String role,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
) {
    public static AdminUserDetailResponse from(User user) {
        return new AdminUserDetailResponse(
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
